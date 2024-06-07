package it.gurzu.swam.iLib.rest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import it.gurzu.swam.iLib.model.UserRole;

import java.util.Date;

public class JWTUtil {

    private static final String SECRET_KEY = "iLib SWAM project";
    private static final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

    public static String generateToken(Long id, String email, UserRole userRole) {
        long expirationTime = 1000 * 3600; // 1 hour in milliseconds
        return JWT.create()
        		.withIssuer("iLib")
        		.withSubject(id.toString())
        		.withClaim("email", email)
        		.withClaim("role", userRole.toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .sign(algorithm);
    }

    public static boolean validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm)
            		.withIssuer("iLib")
            		.build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException exception) {
        	return false;
        }
    }
    
    public static Long getUserIdFromToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            String subject = decodedJWT.getSubject();
            return Long.parseLong(subject);
        } catch (JWTVerificationException | NumberFormatException exception) {
            return null;
        }
    }
    
    public static String getEmailFromToken(String token) {
    	try {
    		DecodedJWT decodedJWT = JWT.decode(token);
    		return decodedJWT.getClaim("email").asString();
    	} catch (JWTVerificationException e) {
    		return null;
    	}
    }
    
    public static UserRole getUserRoleFromToken(String token) {
    	try {
    		DecodedJWT decodedJWT = JWT.decode(token);
    		String userRole = decodedJWT.getClaim("role").asString();
    		if(userRole == null)
    			return null;
    		return UserRole.valueOf(userRole.toUpperCase()); 
		} catch (JWTVerificationException | IllegalArgumentException e) {
			return null;
		}
    }
}
