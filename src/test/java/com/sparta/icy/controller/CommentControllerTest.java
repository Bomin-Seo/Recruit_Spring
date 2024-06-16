package com.sparta.icy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.icy.dto.CommentRequestDto;
import com.sparta.icy.dto.CommentResponseDto;
import com.sparta.icy.entity.Comment;
import com.sparta.icy.exception.EntityNotFoundException;
import com.sparta.icy.repository.NewsfeedRepository;
import com.sparta.icy.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("댓글 작성")
    class writeCommentTest{
        @Test
        @WithMockUser
        @DisplayName("댓글 작성 성공")
        public void writeCommentTest() throws Exception {
            // Given
            Long feedId = 1L;
            CommentRequestDto requestDto = new CommentRequestDto();
            requestDto.setContent("comment-content");

            CommentResponseDto responseDto = new CommentResponseDto(new Comment(requestDto));

            given(commentService.writeComment(feedId, requestDto)).willReturn(responseDto);

            // When
            MvcResult mvcResult = mockMvc.perform(post("/comments/{feedId}", feedId)
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andReturn();
        }

        @Test
        @WithMockUser
        @DisplayName("댓글 작성 실패")
        public void writeCommentFailure() throws Exception {
            // Given
            Long feedId = 1L;
            CommentRequestDto requestDto = new CommentRequestDto();
            requestDto.setContent("comment-content");
            when(commentService.writeComment(anyLong(), any(CommentRequestDto.class))).thenThrow(EntityNotFoundException.class);
            // When
            mockMvc.perform(MockMvcRequestBuilders.post("/comments/{feedId}", feedId)
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(requestDto)))
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        }
    }

    @Test
    @WithMockUser
    public void getCommentsSuccess() throws Exception {
        // Given
        Long feedId = 1L;
        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setContent("comment-content");
        Comment comment = new Comment(requestDto);
        CommentResponseDto responseDto1 = new CommentResponseDto(comment);
        CommentResponseDto responseDto2 = new CommentResponseDto(comment);
        List<CommentResponseDto> responseDtos = Arrays.asList(responseDto1, responseDto2);

        given(commentService.getComments(feedId)).willReturn(responseDtos);

        // When
        ResultActions result = mockMvc.perform(get("/comments/{feedId}", feedId));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value(responseDto1.getContent()))
                .andExpect(jsonPath("$[1].content").value(responseDto2.getContent()));
    }

    @Nested
    @DisplayName("댓글 수정")
    class updateCommentTest{
        @Test
        @DisplayName("댓글 수정 성공")
        @WithMockUser
        public void updateCommentSuccess() throws Exception {
            // Given
            Long commentId = 1L;
            CommentRequestDto requestDto = new CommentRequestDto();
            requestDto.setContent("Updated comment.");

            CommentResponseDto responseDto = new CommentResponseDto(new Comment(requestDto));

            given(commentService.updateComment(commentId, requestDto)).willReturn(responseDto);

            // When
            mockMvc.perform(put("/comments/{commentId}", commentId)
                            .with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.content().string("Comment updated"));
        }

        @Test
        @DisplayName("댓글 수정 실패")
        @WithMockUser
        public void updateCommentFailure() throws Exception {
            // Given
            Long commentId = 1L;
            doThrow(new IllegalArgumentException("댓글 업데이트 권한이 없습니다.")).when(commentService).deleteComment(commentId);
            // When
            ResultActions result = mockMvc.perform(put("/comments/{commentId}", commentId)
                    .with(SecurityMockMvcRequestPostProcessors.csrf()));
            // Then
            result.andExpect(status().isBadRequest());
        }
    }


    @Nested
    @DisplayName("댓글 삭제")
    class deleteCommentTest{
    @Test
    @WithMockUser
    @DisplayName("댓글 삭제 성공")
    public void deleteCommentSuccess() throws Exception {
        // Given
        Long commentId = 1L;
        doNothing().when(commentService).deleteComment(commentId);
        // When
        ResultActions result = mockMvc.perform(delete("/comments/{commentId}", commentId)
                .with(SecurityMockMvcRequestPostProcessors.csrf()));
        // Then
        result.andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("댓글 삭제 실패")
    public void deleteCommentFailure() throws Exception {
        // Given
        Long commentId = 1L;
        doThrow(new IllegalArgumentException("댓글 삭제 권한이 없습니다.")).when(commentService).deleteComment(commentId);
        // When
        ResultActions result = mockMvc.perform(delete("/comments/{commentId}", commentId)
                .with(SecurityMockMvcRequestPostProcessors.csrf()));
        // Then
        result.andExpect(status().isBadRequest());
    }
    }

}
