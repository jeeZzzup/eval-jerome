package com.eex.domain.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class JwtUtilTests {

    @Test
    void invalidTokenReturnsNull() {
        JwtUtil jwtUtil = new JwtUtil("0123456789abcdef0123456789abcdef", 60_000);

        assertNull(jwtUtil.validateAndGetUserId("not-a-jwt"));
    }

    @Test
    void expiredTokenReturnsNull() throws InterruptedException {
        JwtUtil jwtUtil = new JwtUtil("0123456789abcdef0123456789abcdef", 1);
        String token = jwtUtil.generateToken(1);
        Thread.sleep(20);

        assertNull(jwtUtil.validateAndGetUserId(token));
    }
}
