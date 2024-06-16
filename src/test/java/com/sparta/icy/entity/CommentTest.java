package com.sparta.icy.entity;

import com.sparta.icy.dto.CommentRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CommentTest {

    private Comment comment;
    private User user;
    private Newsfeed newsfeed;

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

        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setContent("Test Comment");
        comment = new Comment(commentRequestDto);
        comment.setUser(user);
        comment.setNewsfeed(newsfeed);
    }

    @Test
    public void testCommentCreation() {
        assertNotNull(comment);
        assertEquals("Test Comment", comment.getContent());
        assertEquals(user, comment.getUser());
        assertEquals(newsfeed, comment.getNewsfeed());
    }

    @Test
    public void testSettersAndGetters() {
        comment.setContent("Updated Comment");
        assertEquals("Updated Comment", comment.getContent());
    }
}
