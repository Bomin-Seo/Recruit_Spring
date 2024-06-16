package com.sparta.icy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.icy.config.WebSecurityConfig;
import com.sparta.icy.dto.LoginRequestDto;
import com.sparta.icy.exception.EntityNotFoundException;
import com.sparta.icy.exception.InvalidPasswordException;
import com.sparta.icy.repository.UserRepository;
import com.sparta.icy.service.LogService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@WebMvcTest(
        controllers = LogController.class,
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfig.class)}
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@RequiredArgsConstructor
class LogControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private LogService logService;

    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity(new MockSpringSecurityFilter()))
                .build();
    }

    @Nested
    @DisplayName("로그인 관련 요청")
    class LoginTests {
        @Test
        @DisplayName("로그인 성공")
        void LoginSuccess() throws Exception {
            // Given
            LoginRequestDto loginRequestDto = new LoginRequestDto("username11", "Aa123456789!");

            // When & Then
            mvc.perform(MockMvcRequestBuilders.post("/logs/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(loginRequestDto)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().string("로그인에 성공하였습니다."));

            verify(logService).login(any(LoginRequestDto.class), any(HttpServletResponse.class));
        }

        @Test
        @DisplayName("로그인 실패 - 잘못된 비밀번호")
        void LoginFailure() throws Exception {
            // Given
            LoginRequestDto loginRequestDto = new LoginRequestDto("username11", "invalid_password");
            when(logService.login(any(LoginRequestDto.class), any(HttpServletResponse.class)))
                    .thenThrow(InvalidPasswordException.class);
            // When
            mvc.perform(MockMvcRequestBuilders.post("/logs/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(loginRequestDto)))
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized());
            // Then
            verify(logService).login(any(LoginRequestDto.class), any(HttpServletResponse.class));
        }

        @Test
        @DisplayName("로그인 실패 - 등록되지 않은 사용자")
        void LoginFailure2() throws Exception {
            // Given
            LoginRequestDto loginRequestDto = new LoginRequestDto("non_existing_username", "password");
            when(logService.login(any(LoginRequestDto.class), any(HttpServletResponse.class)))
                    .thenThrow(EntityNotFoundException.class);

            // When
            mvc.perform(MockMvcRequestBuilders.post("/logs/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(loginRequestDto)))
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        }

    }
    @Test
    @DisplayName("로그아웃 요청")
    void logoutSuccess() throws Exception {
        // When
        mvc.perform(MockMvcRequestBuilders.get("/logs/logout"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("로그아웃되었습니다."));
        // Then
        verify(logService).logout(any(HttpServletResponse.class));
    }
}
