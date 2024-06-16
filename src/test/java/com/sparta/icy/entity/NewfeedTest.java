package com.sparta.icy.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NewfeedTest {

    private Newsfeed newsfeed;
    private User user;

    @BeforeEach
    public void setUp() {
        user = new User("username11", "nickname", "Aa123456789!", "test@example.com", "intro", UserStatus.IN_ACTION);
        newsfeed = new Newsfeed();
        newsfeed.setTitle("Test Title");
        newsfeed.setRecruitmentCount(5);
        newsfeed.setContent("Test Content");
        newsfeed.setCreated_at(LocalDateTime.now());
        newsfeed.setUpdated_at(LocalDateTime.now());
        newsfeed.setUser(user);
    }

    @Test
    public void testNewsfeedCreation() {
        assertNotNull(newsfeed);
        assertEquals("Test Title", newsfeed.getTitle());
        assertEquals(5, newsfeed.getRecruitmentCount());
        assertEquals("Test Content", newsfeed.getContent());
        assertEquals(user, newsfeed.getUser());
        assertNotNull(newsfeed.getCreated_at());
        assertNotNull(newsfeed.getUpdated_at());
    }

    @Test
    public void testSettersAndGetters() {
        newsfeed.setTitle("Updated Title");
        assertEquals("Updated Title", newsfeed.getTitle());

        newsfeed.setRecruitmentCount(10);
        assertEquals(10, newsfeed.getRecruitmentCount());

        newsfeed.setContent("Updated Content");
        assertEquals("Updated Content", newsfeed.getContent());

        LocalDateTime now = LocalDateTime.now();
        newsfeed.setUpdated_at(now);
        assertEquals(now, newsfeed.getUpdated_at());
    }
}
