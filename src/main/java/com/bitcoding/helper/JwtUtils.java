package com.bitcoding.helper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

/**
 * @author LongQi-Howard
 */
public class JwtUtils {
    private static final String PUBLIC_KEY = "publickey.txt";

    /**
     * 公钥解析token
     *
     * @param token 用户请求中的token
     * @return Jws<Claims>
     */
    public static Claims parserToken(String token) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        return Jwts.parser().setSigningKey(getPubKey()).parseClaimsJws(token).getBody();
    }

    private static PublicKey getPubKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Resource resource = new ClassPathResource(PUBLIC_KEY);
        InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());
        BufferedReader br = new BufferedReader(inputStreamReader);
        String collect = br.lines().collect(Collectors.joining(""));
        String pem = collect.replaceAll("\\-*BEGIN PUBLIC KEY\\-*", "").replaceAll("\\-*END PUBLIC KEY\\-*", "").replace("\r\n", "").trim();
        byte[] bytes = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }
}
