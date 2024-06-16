package com.sparta.icy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.icy.dto.NewsfeedDto;
import com.sparta.icy.dto.NewsfeedResponseDto;
import com.sparta.icy.entity.Newsfeed;
import com.sparta.icy.service.NewsfeedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NewsfeedController.class)
@AutoConfigureMockMvc
public class NewsfeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsfeedService newsfeedService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("게시물 작성")
    class CreateNewsfeedTest {

        @Test
        @DisplayName("게시물 작성 성공")
        @WithMockUser
        public void createNewsfeedSuccess() throws Exception {
            // Given
            NewsfeedDto requestDto = new NewsfeedDto();
            requestDto.setTitle("제목");
            requestDto.setRecruitmentCount(5);
            requestDto.setContent("내용");

            given(newsfeedService.createNewsfeed(any(NewsfeedDto.class))).willReturn(any(NewsfeedResponseDto.class));
            // When
            mockMvc.perform(post("/boards")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.content().string("뉴스피드가 작성되었습니다."));
        }

        @Test
        @DisplayName("게시물 작성 실패")
        @WithMockUser
        public void createNewsfeedFailure() throws Exception {
            Long commentId = 1L;
            doThrow(new IllegalArgumentException("프로젝트 제목이 비어있습니다.")).when(newsfeedService).createNewsfeed(any(NewsfeedDto.class));
            // When
            ResultActions result = mockMvc.perform(post("/boards")
                    .with(csrf()));
            // Then
            result.andExpect(status().isBadRequest());
        }
    }
    @Nested
    @DisplayName("게시물 조회")
    class getNewsfeedsTest {
        @Test
        @DisplayName("게시물 조회 성공")
        @WithMockUser
        public void getNewsfeedSuccess() throws Exception {
            // Given
            Long feedId = 1L;
            NewsfeedResponseDto expectedResponseDto = new NewsfeedResponseDto();
            expectedResponseDto.setId(feedId);
            expectedResponseDto.setTitle("제목");
            expectedResponseDto.setRecruitmentCount(5);
            expectedResponseDto.setContent("내용");

            given(newsfeedService.getNewsfeed(feedId)).willReturn(expectedResponseDto);

            // When
            mockMvc.perform(get("/boards/{id}", feedId).with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(feedId))
                    .andExpect(jsonPath("$.title").value("제목"))
                    .andExpect(jsonPath("$.recruitmentCount").value(5))
                    .andExpect(jsonPath("$.content").value("내용"));
        }

        @Test
        @DisplayName("게시물 조회 실패")
        @WithMockUser
        public void getNewsfeedFailure() throws Exception {
            Long feedId = 1L;
            given(newsfeedService.getNewsfeed(feedId)).willThrow(new IllegalArgumentException("해당 ID의 게시물을 찾을 수 없습니다: " + feedId));
            mockMvc.perform(get("/boards/{id}", feedId).with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }


    @Nested
    @DisplayName("게시물 수정")
    class UpdateNewsfeedTest {

        @Test
        @DisplayName("게시물 수정 성공")
        @WithMockUser
        public void updateNewsfeedSuccess() throws Exception {
            // Given
            Long feedId = 1L;
            NewsfeedDto requestDto = new NewsfeedDto();
            requestDto.setTitle("수정된 제목");
            requestDto.setRecruitmentCount(10);
            requestDto.setContent("수정된 내용");

            // When
            mockMvc.perform(put("/boards/{feedId}", feedId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("게시물 수정 실패")
        @WithMockUser
        public void updateNewsfeedFailure() throws Exception {

            Long feedId = 1L;
            NewsfeedDto requestDto = new NewsfeedDto();
            requestDto.setTitle("수정된 제목");
            requestDto.setRecruitmentCount(10);
            requestDto.setContent("수정된 내용");

            doThrow(new IllegalArgumentException("게시물 업데이트 권한이 없습니다.")).when(newsfeedService).updateNewsfeed(anyLong(), any(NewsfeedDto.class));

            // When
            mockMvc.perform(put("/boards/{feedId}", feedId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("게시물 삭제")
    class DeleteNewsfeedTest {

        @Test
        @DisplayName("게시물 삭제 성공")
        @WithMockUser
        public void deleteNewsfeedSuccess() throws Exception {
            Long feedId = 1L;
            doNothing().when(newsfeedService).deleteNewsfeed(feedId);
            // When
            mockMvc.perform(delete("/boards/{feedId}", feedId)
                    .with(SecurityMockMvcRequestPostProcessors.csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("게시물 삭제 실패 - 권한 없음")
        @WithMockUser
        public void deleteNewsfeedFailure() throws Exception {
            // Given
            Long feedId = 1L;

            doThrow(new IllegalArgumentException("게시물 삭제 권한이 없습니다.")).when(newsfeedService).deleteNewsfeed(feedId);

            // When
            mockMvc.perform(delete("/boards/{feedId}", feedId).with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("모든 게시물 조회")
    @WithMockUser
    class GetAllNewsfeedTest {

        @Test
        @DisplayName("게시물이 없는 경우")
        public void getAllNewsfeedEmpty() throws Exception {
            // Given
            given(newsfeedService.getAllNewsfeed()).willReturn(Arrays.asList());

            // When
            mockMvc.perform(get("/boards").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.content().string("먼저 작성하여 소식을 알려보세요!"));
        }

        @Test
        @DisplayName("게시물이 있는 경우")
        public void getAllNewsfeedNotEmpty() throws Exception {
            // Given
            NewsfeedResponseDto responseDto1 = new NewsfeedResponseDto();
            responseDto1.setId(1L);
            responseDto1.setTitle("제목1");
            responseDto1.setRecruitmentCount(5);
            responseDto1.setContent("내용1");

            NewsfeedResponseDto responseDto2 = new NewsfeedResponseDto();
            responseDto2.setId(2L);
            responseDto2.setTitle("제목2");
            responseDto2.setRecruitmentCount(3);
            responseDto2.setContent("내용2");

            given(newsfeedService.getAllNewsfeed()).willReturn(Arrays.asList(responseDto1, responseDto2));

            // When
            mockMvc.perform(get("/boards").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].title").value("제목1"))
                    .andExpect(jsonPath("$[0].recruitmentCount").value(5))
                    .andExpect(jsonPath("$[0].content").value("내용1"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].title").value("제목2"))
                    .andExpect(jsonPath("$[1].recruitmentCount").value(3))
                    .andExpect(jsonPath("$[1].content").value("내용2"));
        }
    }
}
