package controller;

import model.Config;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import io.jsonwebtoken.*;

import java.security.Key;
import java.util.Date;


public class JWTProvider {

    /**
     * Method takes the intended subject or payload of the token, creates a new token and returns it as a String.
     * @param subject
     * @return
     */
    public static String createToken(String subject){

        String id = Config.getTokenId();
        String issuer = Config.getTokenIssuer();

        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        byte[] apiKey = DatatypeConverter.parseBase64Binary(Config.getSecret());
        Key signingKey = new SecretKeySpec(apiKey, signatureAlgorithm.getJcaName());


        JwtBuilder builder = Jwts.builder()
                .setId(id)
                .setIssuedAt(now)
                .setSubject(subject)
                .setIssuer(issuer)
                .signWith(signatureAlgorithm, signingKey);


        //set expiration date to two hours from now
        long expMillis = nowMillis + Config.getTokenExpirationTime();
        Date exp = new Date(expMillis);
        builder.setExpiration(exp);

        return builder.compact();

    }

    /**
     * Validates a token and renews it if valid. Otherwise give message depending on the issue with the token
     * @param authorization
     * @return
     */
    public static String validateAndRenewToken(String authorization) {

        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(Config.getSecret()))
                    .parseClaimsJws(authorization).getBody();

            //returning new token if no expiration of token
            return createToken(claims.getSubject());

        }
        catch (ExpiredJwtException e) {
            //if token was valid but is expired, give this message
            return "{\"message\":\"Your session has expired. Please login again\"}";
        }
        catch (InvalidClaimException | SignatureException | MalformedJwtException | IllegalArgumentException e) {
            //if token invalid, give message of access denied
            return "{\"message\":\"Access denied\"}";
        }
    }

    /**
     * Will be used to get id when handling expiration is set up properly
     * @param authorization
     * @return
     */
    public static int getUserId(String authorization){


        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(Config.getSecret()))
                    .parseClaimsJws(authorization).getBody();

            String [] values = claims.getSubject().split("---");

            return Integer.valueOf(values[1]);
        }
        catch (ExpiredJwtException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
