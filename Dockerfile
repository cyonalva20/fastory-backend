# ----------------------------------------------------------------------
# ETAPA 1: BUILD (Compilación)
# Utilizamos la imagen oficial y recomendada de Eclipse Temurin JDK 17.
FROM eclipse-temurin:17-jdk-jammy AS build

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar archivos esenciales para la compilación (pom.xml y Maven Wrapper)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Descargar dependencias
RUN ./mvnw dependency:go-offline -B

# Copiar el resto del código fuente
COPY src src

# Compilar el proyecto y empaquetar el JAR.
# CORRECCIÓN CRÍTICA: Movemos el archivo JAR generado (que tiene el nombre largo) 
# a un nombre fijo (target/app.jar) ANTES de la etapa de ejecución.
RUN ./mvnw clean package -DskipTests && mv target/*.jar target/app.jar

# ----------------------------------------------------------------------

# ETAPA 2: RUNTIME (Ejecución)
# Usamos Eclipse Temurin JRE 17, una imagen ligera para la ejecución final.
FROM eclipse-temurin:17-jre-jammy

# Directorio de trabajo
WORKDIR /app

# Copia el JAR con el nombre fijo 'app.jar' desde la etapa de build
# NOTA: Ahora copiamos "app.jar", no el comodín.
COPY --from=build /app/target/app.jar app.jar

# 1. Documentación: Puerto de escucha por defecto de Spring Boot
EXPOSE 8080

# 2. COMANDO DE EJECUCIÓN (CMD)
# CRÍTICO: Ejecuta el archivo con el nombre fijo 'app.jar', usando el puerto dinámico de Railway ($PORT).
CMD java -Dserver.port=${PORT:-8080} -jar app.jar
