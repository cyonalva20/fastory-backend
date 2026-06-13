package com.fastory.fastorybackend.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private Key getSigningKey() {
        // Asegurar que la clave tenga al menos 32 bytes para HS256
        String paddedKey = secretKey;
        while (paddedKey.getBytes().length < 32) {
            paddedKey = paddedKey + paddedKey;
        }
        return Keys.hmacShaKeyFor(paddedKey.getBytes());
    }

    /**
     * Genera un JWT que incluye username, rol e idEmpresa como claims.
     * El idEmpresa es esencial para el filtro de tenant en cada request.
     */
    public String generarToken(String username, String rol, Integer idEmpresa, String nombreEmpresa) {
        return Jwts.builder()
                .setSubject(username)
                .claim("rol", rol)
                .claim("idEmpresa", idEmpresa)
                .claim("nombreEmpresa", nombreEmpresa)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String obtenerUsername(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            logger.error("Error al obtener username del token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extrae el idEmpresa (tenant) del token JWT.
     * Retorna null si el token no contiene el claim o es inválido.
     */
    public Integer obtenerIdEmpresa(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("idEmpresa", Integer.class);
        } catch (JwtException e) {
            logger.error("Error al obtener idEmpresa del token: " + e.getMessage());
            return null;
        }
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            logger.error("Firma JWT inválida: " + e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Token JWT malformado: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Token JWT expirado: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT no soportado: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Token JWT vacío: " + e.getMessage());
        }
        return false;
    }
}