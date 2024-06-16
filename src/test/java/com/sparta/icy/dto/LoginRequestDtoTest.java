package com.sparta.icy.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginRequestDtoTest {

    @Test
    public void testLoginRequestDtoCreation() {
        LoginRequestDto dto = new LoginRequestDto("username", "password");

        assertNotNull(dto);
        assertEquals("username", dto.getUsername());
        assertEquals("password", dto.getPassword());
    }
}