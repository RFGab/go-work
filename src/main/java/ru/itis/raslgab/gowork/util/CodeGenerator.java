package ru.itis.raslgab.gowork.util;

import java.security.SecureRandom;

public final class CodeGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // Убрал I, l, 1, O, 0 для удобства ручного ввода из письма
    private static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";

    public static String generate(int length) {
        if (length <= 0) throw new IllegalArgumentException("длина должна быть > 0");
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(ALPHABET.charAt(SECURE_RANDOM.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }

    // готовый метод для 6значного кода
    public static String generate6Char() {
        return generate(6);
    }
}