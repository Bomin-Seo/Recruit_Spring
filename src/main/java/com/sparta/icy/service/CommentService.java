package com.sparta.icy.service;

import com.sparta.icy.dto.CommentRequestDto;
import com.sparta.icy.dto.CommentResponseDto;
import com.sparta.icy.entity.Comment;
import com.sparta.icy.entity.Newsfeed;
import com.sparta.icy.entity.User;
import com.sparta.icy.repository.CommentRepository;
import com.sparta.icy.repository.NewsfeedRepository;
import com.sparta.icy.security.UserDetailsImpl;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final NewsfeedService newsfeedService;
    private final NewsfeedRepository newsfeedRepository;
    MessageSource messageSource;

    public CommentService(CommentRepository commentRepository, NewsfeedService newsfeedService, NewsfeedRepository newsfeedRepository) {
        this.commentRepository = commentRepository;
        this.newsfeedService = newsfeedService;
        this.newsfeedRepository = newsfeedRepository;
    }

    public CommentResponseDto writeComment(Long feed_id, CommentRequestDto requestDto) {
        User currentUser = getUser();
        Newsfeed newsfeed = newsfeedRepository.findById(feed_id)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageSource.getMessage("entity.not.found.user", null,
                                "해당 사용자가 없습니다.", Locale.getDefault())));

        Comment comment = new Comment(requestDto);
        comment.setContent(requestDto.getContent());
        comment.setCreated_at(LocalDateTime.now());
        comment.setUpdated_at(LocalDateTime.now());
        comment.setUser(currentUser);
        comment.setNewsfeed(newsfeed);
        Comment savedcomment = commentRepository.save(comment);
        return new CommentResponseDto(savedcomment);
    }

    public List<CommentResponseDto> getComments(Long feed_id) {
        List<Comment> comments = commentRepository.findByNewsfeedId(feed_id);
        return comments.stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }

    public CommentResponseDto updateComment(Long comments_id, CommentRequestDto requestDto) {
        User currentUser = getUser();
        Comment comment = commentRepository.findById(comments_id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 게시물을 찾을 수 없습니다: " + comments_id));

        if (!currentUser.getId().equals(comment.getUser().getId())) {
            throw new IllegalArgumentException("댓글 업데이트 권한이 없습니다.");
        }
        comment.setContent(requestDto.getContent());
        comment.setUpdated_at(LocalDateTime.now());
        commentRepository.save(comment);
        return new CommentResponseDto(comment);
    }

    public void deleteComment(Long comments_id) {
        User currentUser = getUser();
        Comment comment = commentRepository.findById(comments_id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 게시물을 찾을 수 없습니다: " + comments_id));

        if (!currentUser.getId().equals(comment.getUser().getId())) {
            throw new IllegalArgumentException("게시물 삭제 권한이 없습니다.");
        }
        commentRepository.delete(comment);
    }

    private static User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }

        // Principal이 UserDetailsImpl 타입인지 확인
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) {
            throw new IllegalStateException("사용자 정보를 가져올 수 없습니다.");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) principal;
        User currentUser = userDetails.getUser();
        return currentUser;
    }
}
