package com.sparta.icy.entity;

import com.sparta.icy.dto.UserUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User("username11", "nickname", "Aa123456789!", "test@example.com", "intro", UserStatus.IN_ACTION);
    }

    @Test
    public void testUserCreation() {
        assertNotNull(user);
        assertEquals("username11", user.getUsername());
        assertEquals("nickname", user.getNickname());
        assertEquals("Aa123456789!", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("intro", user.getIntro());
        assertEquals(UserStatus.IN_ACTION.getStatus(), user.getStatus());
    }

    @Test
    public void testUpdateUser() {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setNickname("newNickname");
        updateRequest.setIntro("newIntro");
        updateRequest.setNewPassword("newPassword");

        user.update(updateRequest);

        assertEquals("newNickname", user.getNickname());
        assertEquals("newIntro", user.getIntro());
        assertEquals("newPassword", user.getPassword());
    }
}