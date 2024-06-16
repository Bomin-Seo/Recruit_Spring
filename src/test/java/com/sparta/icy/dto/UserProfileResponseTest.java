package com.sparta.icy.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserProfileResponseTest {

    @Test
    public void testUserProfileResponseCreation() {
        UserProfileResponse dto = new UserProfileResponse("username", "nickname", "email@example.com", "intro");

        assertNotNull(dto);
        assertEquals("username", dto.getUsername());
        assertEquals("nickname", dto.getNickname());
        assertEquals("email@example.com", dto.getEmail());
        assertEquals("intro", dto.getIntro());
    }
}