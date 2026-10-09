package com.hscmt.common.util;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public class CryptoUtil {

    /**
     * 문자열 SHA256 암호화
     * @param inputStr 사용자 입력 문자열
     * @param salt : salt
     * @return inputStr + salt -> hash
     */
    public static String encryptSHA256 (String inputStr, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String combinedStr = inputStr + salt; /* 입력 문자열 + salt 값 */
            byte[] digest = md.digest(combinedStr.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("encryptSHA256 error : {}", e.getMessage());
            throw new RuntimeException("encryptSHA256 error : " + e.getMessage() );
        }
    }

    /**
     * salt 값 생성
     * @return 16byte base64Encode salt
     */
    public static String createSalt () {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 입력문자열 hash 비교
     * @param inputStr : 사용자 입력 문자열
     * @param salt : salt
     * @param encodeStr : 해쉬 된 문자열
     * @return true or false
     */
    public static boolean isMatched (String inputStr, String salt, String encodeStr) {
        return encryptSHA256(inputStr, salt).equals(encodeStr) ? true : false;
    }
}
