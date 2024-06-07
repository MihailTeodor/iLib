package it.gurzu.swam.iLib.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtils {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String hashPassword(String plainTextPassword) {
        return encoder.encode(plainTextPassword);
    }
}
