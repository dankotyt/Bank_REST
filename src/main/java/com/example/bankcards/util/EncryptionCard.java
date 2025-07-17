//package com.example.bankcards.util;
//
//import com.example.bankcards.exception.auth.EncryptionException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.Cipher;
//import javax.crypto.SecretKey;
//import javax.crypto.SecretKeyFactory;
//import javax.crypto.spec.PBEKeySpec;
//import javax.crypto.spec.SecretKeySpec;
//import java.nio.charset.StandardCharsets;
//import java.security.spec.KeySpec;
//import java.util.Base64;
//
 // Доп. фича: хэширования данных карт для проверки на коллизии
//
//@Component
//@RequiredArgsConstructor
//public class EncryptionCard {
//    @Value("${encryption.salt}")
//    private String salt;
//    @Value("${encryption.secret}")
//    private String secret;
//
//    public String encrypt(String data) {
//        try {
//            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
//            KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt.getBytes(), 65536, 256);
//            SecretKey tmp = factory.generateSecret(spec);
//            SecretKey secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
//
//            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
//            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//            byte[] iv = cipher.getIV();
//            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
//
//            byte[] combined = new byte[iv.length + encrypted.length];
//            System.arraycopy(iv, 0, combined, 0, iv.length);
//            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
//
//            return Base64.getEncoder().encodeToString(combined);
//        } catch (Exception e) {
//            throw new EncryptionException("Failed to encrypt data: " + e);
//        }
//    }
//}
