package com.sparta.icy.controller;

import com.sparta.icy.dto.CommentRequestDto;
import com.sparta.icy.dto.CommentResponseDto;
import com.sparta.icy.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{feedId}")
    public CommentResponseDto writeComment(@PathVariable Long feedId, @RequestBody CommentRequestDto requestDto) {
        return commentService.writeComment(feedId, requestDto);
    }

    @GetMapping("/{feedId}")
    public List<CommentResponseDto> getComments(@PathVariable Long feedId) {
        return commentService.getComments(feedId);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<String> updateComment(@PathVariable Long commentId, @RequestBody CommentRequestDto requestDto) {
        commentService.updateComment(commentId, requestDto);
        return ResponseEntity.ok("Comment updated");
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
    }

}
