package com.dnastack.ddap.explore.common.session;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.http.ResponseCookie;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;

public class SessionEncryptionUtils {

    public static final String COOKIE_NAME = "SESSION_DECRYPTION_KEY";
    public static final String SESSION_ENCRYPT_KEY_NAME = "_session_encryption_key_";

    public static void setSessionEncryption(ServerWebExchange exchange, WebSession session) {
        KeyPair keyPair = createNewKeyPair();
        session.getAttributes().put(SESSION_ENCRYPT_KEY_NAME, keyPair.getPublic());
        String encodedPrivateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(COOKIE_NAME, encodedPrivateKey)
            .path(exchange.getRequest().getPath().contextPath().value() + "/")
            .maxAge(Duration.ofSeconds(-1))
            .httpOnly(true)
            .secure("https".equalsIgnoreCase(exchange.getRequest().getURI().getScheme()))
            .sameSite("Lax");

        exchange.getResponse().getCookies().set(COOKIE_NAME, cookieBuilder.build());
    }

    public static void expireSessionDecryption(ServerWebExchange exchange){
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(COOKIE_NAME, "")
            .path(exchange.getRequest().getPath().contextPath().value() + "/")
            .maxAge(Duration.ZERO)
            .httpOnly(true)
            .secure("https".equalsIgnoreCase(exchange.getRequest().getURI().getScheme()))
            .sameSite("Lax");
        exchange.getResponse().getCookies().set(COOKIE_NAME, cookieBuilder.build());
    }

    public static KeyPair createNewKeyPair() {
        try {
            RSAKey key = new RSAKeyGenerator(2048)
                .keyUse(KeyUse.ENCRYPTION)
                .keyID(UUID.randomUUID().toString())
                .generate();

            return key.toKeyPair();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encryptData(WebSession session, String content) {
        return encryptData((PublicKey) session.getAttribute(SESSION_ENCRYPT_KEY_NAME), content);
    }

    public static String encryptData(@NonNull PublicKey publicKey, @NonNull String content) {
        try {
            RSAEncrypter rsaEncrypter = new RSAEncrypter((RSAPublicKey) publicKey);
            JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
                .contentType("JWE")
                .build();
            Payload payload = new Payload(content);
            JWEObject jweObject = new JWEObject(header, payload);
            jweObject.encrypt(rsaEncrypter);
            return jweObject.serialize();
        } catch (JOSEException e) {
            throw new IllegalArgumentException(e);
        }
    }


    public static String decryptData(String b64PKCS8PrvateKey, String cipher) {
        try {
            byte[] decodedPrivateKey = Base64.getDecoder().decode(b64PKCS8PrvateKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodedPrivateKey));
            return decryptData(privateKey, cipher);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String decryptData(PrivateKey privateKey, String cipher) {
        try {
            RSADecrypter decrypter = new RSADecrypter(privateKey);
            JWEObject jweObject = JWEObject.parse(cipher);
            jweObject.decrypt(decrypter);
            return jweObject.getPayload().toString();
        } catch (JOSEException | ParseException e) {
            throw new IllegalArgumentException(e);
        }

    }
}
