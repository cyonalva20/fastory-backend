$ErrorActionPreference = 'Stop'

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "🚀 Iniciando Despliegue de Backend Fastory 🚀" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

# 1. Configurar JWT Secret si está vacío
Write-Host "`n[1/5] Verificando secretos en AWS..." -ForegroundColor Yellow
$jwtSecretArn = "fastory/jwt-secret"
$ErrorActionPreference = 'Continue'
$jwtCheck = aws secretsmanager get-secret-value --secret-id $jwtSecretArn 2>&1
$ErrorActionPreference = 'Stop'

if ($jwtCheck -match "ResourceNotFoundException" -or $LASTEXITCODE -ne 0) {
    $randomJwt = -join ((48..57) + (65..90) + (97..122) | Get-Random -Count 32 | % {[char]$_})
    aws secretsmanager put-secret-value --secret-id $jwtSecretArn --secret-string $randomJwt | Out-Null
    Write-Host "✅ Se generó y guardó un nuevo JWT_SECRET en AWS." -ForegroundColor Green
} else {
    Write-Host "✅ El JWT_SECRET ya existe en AWS." -ForegroundColor Green
}

# 2. Construir la imagen Docker
Write-Host "`n[2/5] Construyendo imagen Docker localmente..." -ForegroundColor Yellow
docker build -t fastory-backend:latest .
if ($LASTEXITCODE -ne 0) { throw "Error al construir la imagen Docker." }
Write-Host "✅ Imagen Docker construida con éxito." -ForegroundColor Green

# 3. Exportar imagen a .tar
Write-Host "`n[3/5] Comprimiendo imagen (esto puede tardar un poco)..." -ForegroundColor Yellow
docker save -o backend.tar fastory-backend:latest
if ($LASTEXITCODE -ne 0) { throw "Error al guardar la imagen." }
Write-Host "✅ Archivo backend.tar creado." -ForegroundColor Green

# 4. Subir a S3
$s3Bucket = "fastory-production-frontend"
Write-Host "`n[4/5] Subiendo imagen a s3://$s3Bucket/backend.tar..." -ForegroundColor Yellow
aws s3 cp backend.tar s3://$s3Bucket/backend.tar
if ($LASTEXITCODE -ne 0) { throw "Error al subir a S3." }
Write-Host "✅ Subida completada." -ForegroundColor Green

# 5. Ejecutar despliegue vía AWS SSM
Write-Host "`n[5/5] Enviando orden de despliegue a las instancias EC2..." -ForegroundColor Yellow

$ssmCommand = @'
#!/bin/bash
echo '--- 1. Instalando Docker si no existe ---'
if ! command -v docker &> /dev/null; then
    sudo yum update -y
    sudo yum install -y docker jq
    sudo systemctl enable docker
    sudo systemctl start docker
    sudo usermod -aG docker ec2-user
fi

echo '--- 2. Obteniendo credenciales de la BD y JWT ---'
REGION="us-east-1"
RDS_SECRET=$(aws secretsmanager get-secret-value --secret-id fastory-production/rds-proxy-credentials --region $REGION --query SecretString --output text)
JWT_SECRET=$(aws secretsmanager get-secret-value --secret-id fastory/jwt-secret --region $REGION --query SecretString --output text)

# Extraer y decodificar correctamente los valores con jq
DB_USER=$(echo $RDS_SECRET | jq -r '.username')
DB_PASS=$(echo $RDS_SECRET | jq -r '.password')
DB_HOST=$(echo $RDS_SECRET | jq -r '.host')
DB_PORT=$(echo $RDS_SECRET | jq -r '.port')
DB_NAME=$(echo $RDS_SECRET | jq -r '.dbname')

echo '--- 3. Descargando imagen desde S3 ---'
aws s3 cp s3://S3_BUCKET_PLACEHOLDER/backend.tar /tmp/backend.tar

echo '--- 4. Cargando imagen en Docker ---'
sudo docker load -i /tmp/backend.tar

echo '--- 5. Deteniendo contenedor anterior si existe ---'
sudo docker stop fastory-backend || true
sudo docker rm fastory-backend || true

echo '--- 6. Levantando nuevo contenedor ---'
sudo docker run -d \
  --name fastory-backend \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME" \
  -e SPRING_DATASOURCE_USERNAME="$DB_USER" \
  -e SPRING_DATASOURCE_PASSWORD="$DB_PASS" \
  -e JWT_SECRET="$JWT_SECRET" \
  -e PORT="8080" \
  fastory-backend:latest

echo '--- Despliegue Finalizado ---'
'@

$ssmCommand = $ssmCommand -replace "S3_BUCKET_PLACEHOLDER", $s3Bucket


$ssmPayload = @{
    DocumentName = "AWS-RunShellScript"
    Targets = @(
        @{
            Key = "InstanceIds"
            Values = @("i-0d5815561fbc5f5d2")
        }
    )
    Parameters = @{
        commands = @($ssmCommand)
    }
    TimeoutSeconds = 600
}

$ssmPayload | ConvertTo-Json -Depth 10 | Out-File -FilePath "ssm_payload.json" -Encoding ASCII

$commandId = (aws ssm send-command --cli-input-json file://ssm_payload.json --query "Command.CommandId" --output text)

if (-not $commandId) { throw "Error al enviar el comando SSM." }

Write-Host "✅ Comando enviado con ID: $commandId" -ForegroundColor Green
Write-Host "Esperando a que las instancias terminen de procesar (puede tomar un par de minutos)..." -ForegroundColor Yellow

# Esperar a que termine (polling simple)
while ($true) {
    Start-Sleep -Seconds 10
    $status = aws ssm list-commands --command-id $commandId --query "Commands[0].Status" --output text
    if ($status -eq "Success") {
        Write-Host "🚀 ¡Despliegue exitoso en todas las instancias!" -ForegroundColor Green
        break
    } elseif ($status -eq "Failed" -or $status -eq "Cancelled" -or $status -eq "TimedOut") {
        Write-Host "❌ El despliegue falló. Revisa los logs en la consola de AWS Systems Manager." -ForegroundColor Red
        break
    } else {
        Write-Host "Estado actual: $status..."
    }
}

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "             Proceso Finalizado              " -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
