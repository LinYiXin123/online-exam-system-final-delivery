package com.rabbiter.oes.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String encode(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        return encoder.encode(rawPassword);
    }

    public boolean isEncoded(String password) {
        return password != null && password.startsWith("$2a$")
                || password != null && password.startsWith("$2b$")
                || password != null && password.startsWith("$2y$");
    }

    public boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (isEncoded(storedPassword)) {
            return encoder.matches(rawPassword, storedPassword);
        }
        return rawPassword.equals(storedPassword);
    }

    public String encodeIfNeeded(String password) {
        if (password == null || password.trim().isEmpty()) {
            return password;
        }
        return isEncoded(password) ? password : encode(password);
    }
}
