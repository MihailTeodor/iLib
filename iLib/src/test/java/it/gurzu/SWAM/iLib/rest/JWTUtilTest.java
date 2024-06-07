package it.gurzu.SWAM.iLib.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import it.gurzu.swam.iLib.model.UserRole;
import it.gurzu.swam.iLib.rest.JWTUtil;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JWTUtilTest {

    private static String validEmail;
    private static UserRole validRole;
    private static Long validUserId;
    
    private static final String secretKey = " iLib SWAM project";
    private static final Algorithm algorithm = Algorithm.HMAC256(secretKey);

    @BeforeAll
    public static void setUp() {
        validEmail = "user@example.com";
        validRole = UserRole.ADMINISTRATOR;
        validUserId = 1L;
    }
    
    @Test
    public void testGenerateToken() {
        String token = JWTUtil.generateToken(validUserId, validEmail, validRole);
        DecodedJWT decoded = JWT.decode(token);
        
        assertEquals(validUserId.toString(), decoded.getSubject());
        assertEquals(validEmail, decoded.getClaim("email").asString());
        assertEquals(validRole.toString(), decoded.getClaim("role").asString());
        assertNotNull(decoded.getIssuedAt());
        assertNotNull(decoded.getExpiresAt());
    }
    
    @Test
    public void testTokenExpirationIsSetCorrectly() {
        String token = JWTUtil.generateToken(validUserId, validEmail, validRole);
        DecodedJWT decoded = JWT.decode(token);
        long expectedDuration = 3600 * 1000;
        long actualDuration = decoded.getExpiresAt().getTime() - decoded.getIssuedAt().getTime();

        assertEquals(expectedDuration, actualDuration);
    }

    @Test
    public void testValidateToken() {
        String token = JWTUtil.generateToken(validUserId, validEmail, validRole);
        
        assertTrue(JWTUtil.validateToken(token));
    }

    @Test
    public void testValidateToken_WithInvalidIssuer() {
        String token = generateTokenWithCustomIssuer("invalidIssuer");
        
        assertFalse(JWTUtil.validateToken(token));
    }

    @Test
    public void testValidateToken_WithExpiredToken() {
        long oneHourAgo = System.currentTimeMillis() - 1000 * 60 * 60;
        String token = JWT.create()
            .withIssuer("iLib")
            .withSubject(validUserId.toString())
            .withClaim("email", validEmail)
            .withClaim("role", validRole.toString())
            .withIssuedAt(new Date(oneHourAgo))
            .withExpiresAt(new Date(oneHourAgo + 1000 * 60)) // Expired
            .sign(algorithm);

        assertFalse(JWTUtil.validateToken(token));
    }

    @Test
    public void testValidateToken_WithInvalidToken() {
        String token = "invalid.token.here";
        
        assertFalse(JWTUtil.validateToken(token));
    }

    @Test
    public void testValidateToken_WithEmptyToken() {
    	String token = "";
    	
        assertFalse(JWTUtil.validateToken(token));
    }
    
    @Test
    public void testGetUserIdFromToken_WithValidToken() {
    	String validToken = JWT.create()
                .withSubject(validUserId.toString())
                .sign(algorithm);
    	
        Long userId = JWTUtil.getUserIdFromToken(validToken);
        
        assertEquals(validUserId, userId);
    }

    @Test
    public void testGetUserIdFromTokenWithMalformedToken() {
    	String malformedToken = "not.valid.token";
    	
    	Long userId = JWTUtil.getUserIdFromToken(malformedToken);
    	
    	assertNull(userId);
    }
    
    @Test
    public void testGetUserIdFromToken_WithNonNumericSubject() {
        String invalidToken = JWT.create()
                .withSubject("invalid user id")
                .sign(algorithm);
        
        Long userId = JWTUtil.getUserIdFromToken(invalidToken);
        
        assertNull(userId);
    }


    @Test
    public void testGetEmailFromToken_WithValidToken() {
    	String validToken = JWT.create()
                .withClaim("email", validEmail)
                .sign(algorithm);
    	
        String email = JWTUtil.getEmailFromToken(validToken);
        
        assertEquals(validEmail, email);
    }

    @Test
    public void testGetEmailFromToken_WithoutEmailClaim() {
        String tokenWithoutEmail = JWT.create().sign(algorithm);
        
        String email = JWTUtil.getEmailFromToken(tokenWithoutEmail);
        
        assertNull(email);
    }

    @Test
    public void testGetEmailFromToken_WithMalformedToken() {
        String malformedToken = "not.valid.token";
        
        String email = JWTUtil.getEmailFromToken(malformedToken);
        
        assertNull(email);
    }
    
    @Test
    public void testGetUserRoleFromToken_WithValidRole() {
        String validToken = JWT.create()
                .withClaim("role", UserRole.ADMINISTRATOR.toString())
                .sign(algorithm);
        
        UserRole role = JWTUtil.getUserRoleFromToken(validToken);
        
        assertEquals(UserRole.ADMINISTRATOR, role);
    }

    @Test
    public void testGetUserRoleFromToken_WithoutRoleClaim() {
        String tokenWithoutRole = JWT.create().sign(algorithm);

        UserRole role = JWTUtil.getUserRoleFromToken(tokenWithoutRole);
        
        assertNull(role);
    }
    
    @Test
    public void testGetUserRoleFromToken_WithMalformedToken() {
        String malformedToken = "not.valid.token";
        
        UserRole role = JWTUtil.getUserRoleFromToken(malformedToken);
        
        assertNull(role);
    }

    @Test
    public void testGetUserRoleFromTokenWithInvalidRole() {
        String tokenWithInvalidRole = JWT.create()
                                    .withClaim("role", "NOT_A_REAL_ROLE")
                                    .sign(algorithm);
        
        UserRole role = JWTUtil.getUserRoleFromToken(tokenWithInvalidRole);
        
        assertNull(role);
    }
    
    private String generateTokenWithCustomIssuer(String issuer) {
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(validUserId.toString())
                .withClaim("email", validEmail)
                .withClaim("role", validRole.toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                .sign(algorithm);
    }
}
