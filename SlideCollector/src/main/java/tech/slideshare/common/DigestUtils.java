package tech.slideshare.common;

import jakarta.xml.bind.DatatypeConverter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtils {

    public static String getSHA1(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));

            return DatatypeConverter.printHexBinary(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        }
    }
}
