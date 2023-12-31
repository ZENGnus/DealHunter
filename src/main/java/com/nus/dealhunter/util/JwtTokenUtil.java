package com.nus.dealhunter.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.nus.dealhunter.model.CustomUserDetails;

@Component
public class JwtTokenUtil {

  private final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

  @Value("${app.jwtSecret}")
  private String jwtSecret;


  @Value("${app.jwtExpirationInMs}")
  private int jwtExpirationInMs;


  public String generateToken(Authentication authentication) {

    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

    return Jwts.builder()
        .setSubject(Long.toString(userDetails.getId()))
        .setIssuedAt(new Date())
        .setExpiration(expiryDate)
        .signWith(SignatureAlgorithm.HS256, jwtSecret)
        .compact();
  }

  public Long getUserIdFromJWT(String token) {
    Claims claims = Jwts.parser()
        .setSigningKey(jwtSecret)
        .parseClaimsJws(token)
        .getBody();

    return Long.parseLong(claims.getSubject());
  }

  public boolean validateToken(String authToken) {
    try {
      Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
      return true;
    } catch (SignatureException ex) {
      logger.error("Invalid JWT signature");
    } catch (MalformedJwtException ex) {
      logger.error("Invalid JWT token");
    } catch (ExpiredJwtException ex) {
      logger.error("Expired JWT token");
    } catch (UnsupportedJwtException ex) {
      logger.error("Unsupported JWT token");
    } catch (IllegalArgumentException ex) {
      logger.error("JWT claims string is empty.");
    }
    return false;
  }
}
