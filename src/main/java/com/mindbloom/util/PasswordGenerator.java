package com.mindbloom.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println("Admin password hash:");
        System.out.println(encoder.encode("1234"));

        System.out.println("\nCounselor password hash:");
        System.out.println(encoder.encode("1234"));
    }
}
