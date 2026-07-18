package br.edu.ufape.enzitech.security;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${api.security.token.secret}")
    private String secretKey;

    @Value("${api.security.token.expiration_time:86400000}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);

        boolean isUsernameMatch = username.equals(userDetails.getUsername());
        boolean isTokenExpired = isTokenExpired(token);

        boolean isTokenRevoked = false;
        
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            isTokenRevoked = isTokenIssuedBeforeCredentialsUpdate(token, customUserDetails);
        }

        return isUsernameMatch && !isTokenExpired && !isTokenRevoked;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private boolean isTokenIssuedBeforeCredentialsUpdate(String token, CustomUserDetails userDetails) {
        LocalDateTime credentialsUpdatedAt = userDetails.getUser().getCredentialsUpdatedAt();

        if (credentialsUpdatedAt == null) {
            return false;
        }

        Date issuedAt = extractClaim(token, io.jsonwebtoken.Claims::getIssuedAt);
        if (issuedAt == null) {
            return false; 
        }

        Date credentialsUpdateDate = java.util.Date.from(
                credentialsUpdatedAt.atZone(java.time.ZoneId.systemDefault()).toInstant()
        );

        return issuedAt.before(credentialsUpdateDate);
    }
}