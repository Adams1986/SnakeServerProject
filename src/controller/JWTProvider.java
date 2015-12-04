package controller;

import model.Config;
import model.User;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import io.jsonwebtoken.*;

import java.security.Key;
import java.util.Date;

/**
 * Created by simonadams on 04/12/15.
 */
public class JWTProvider {

    public static String createToken(User user){

        String subject = user.getEmail()+"---"+user.getId();
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
        long expMillis = nowMillis + 60000;
//        long expMillis = nowMillis + 7200000;
        Date exp = new Date(expMillis);
        System.out.println(exp);
        builder.setExpiration(exp);

        return builder.compact();

    }

    public static void validateToken(String authorization) {

        Claims claims = Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(Config.getSecret()))
                .parseClaimsJws(authorization).getBody();

        System.out.println("ID: " + claims.getId());
        System.out.println("Subject: " + claims.getSubject());
        System.out.println("Issuer: " + claims.getIssuer());
        System.out.println("Expiration: " + claims.getExpiration());
    }

    public static int getUserId(String authorization){

        Claims claims = Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(Config.getSecret()))
                .parseClaimsJws(authorization).getBody();

        String [] values = claims.getSubject().split("---");

        return Integer.valueOf(values[1]);
    }
}
