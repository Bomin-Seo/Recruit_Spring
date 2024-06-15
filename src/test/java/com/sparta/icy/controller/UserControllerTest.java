package com.sparta.icy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.icy.config.WebSecurityConfig;
import com.sparta.icy.dto.SignoutRequestDto;
import com.sparta.icy.dto.SignupRequestDto;
import com.sparta.icy.dto.UserProfileResponse;
import com.sparta.icy.dto.UserUpdateRequest;
import com.sparta.icy.entity.User;
import com.sparta.icy.entity.UserStatus;
import com.sparta.icy.repository.UserRepository;
import com.sparta.icy.security.UserDetailsImpl;
import com.sparta.icy.service.LogService;
import com.sparta.icy.service.UserService;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WebMvcTest(
        controllers = {UserController.class, LogController.class},
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebSecurityConfig.class)}
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ObjectMapper objectMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @MockBean
    UserService userService;

    @MockBean
    LogService logService;

    @MockBean
    UserRepository userRepository;

    private SignupRequestDto signupRequestDto;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity(new MockSpringSecurityFilter()))
                .build();
    }

    @Nested
    @Order(1)
    @DisplayName("회원 가입 요청 처리")
    class signup {
        @BeforeEach
        void setUp() {
            signupRequestDto = new SignupRequestDto();
            signupRequestDto.setUsername("username11");
            signupRequestDto.setNickname("nickname");
            signupRequestDto.setPassword("Aa123456789!");
            signupRequestDto.setEmail("test@sparta.com");
            signupRequestDto.setIntro("hi");
        }
        @Test
        @DisplayName("회원 가입 성공")
        void SignupSuccess() throws Exception {
            // when
            ResultActions resultActions = mvc.perform(post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequestDto)));
            // then
            resultActions.andExpect(status().isOk())
                    .andExpect(content().string("회원 가입 성공"));

            assertEquals("회원 가입 성공", resultActions.andReturn().getResponse().getContentAsString(),
                    () -> "비밀번호 작성 조건 혹은 빈 필드가 없는지 확인해주세요.");
        }

        @Test
        @DisplayName("회원 가입 실패 - 비밀번호 입력 조건 미준수")
        void SignupFail() throws Exception {
            // given
            signupRequestDto.setUsername("username12");
            signupRequestDto.setPassword("A123456789");
            // when
            ResultActions resultActions = mvc.perform(post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequestDto)));
            // then
            resultActions.andExpect(status().isOk())
                    .andExpect(content().string("회원 가입 실패"));
        }

        @Test
        @DisplayName("회원 가입 실패 - 입력되지 않은 field 존재")
        void SignupFail2() throws Exception {
            // given
            signupRequestDto.setEmail("");
            signupRequestDto.setIntro("");
            // when
            ResultActions resultActions = mvc.perform(post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequestDto)));
            // then
            resultActions.andExpect(status().isOk())
                    .andExpect(content().string("회원 가입 실패"));
        }
    }

    @Nested
    @DisplayName("유저 프로필 반환")
    class getUser {

        @Test
        @DisplayName("유저 프로필 반환 성공")
        public void getUserByIdSuccess() throws Exception {
            // Given
            long userId = 1L;
            UserProfileResponse expectedProfile = new UserProfileResponse("username11", "nickname", "intro", "test@test.com");
            when(userService.getUser(userId)).thenReturn(expectedProfile);

            // When, Then
            mvc.perform(MockMvcRequestBuilders.get("/users/{id}", userId))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.username").value(expectedProfile.getUsername()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.nickname").value(expectedProfile.getNickname()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.intro").value(expectedProfile.getIntro()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(expectedProfile.getEmail()));
        }

        @Test
        @DisplayName("유저 프로필 반환 실패 - 사용자가 없는 경우")
        public void getUserById_userNotFound() throws Exception {
            // Given
            long userId = 999L;
            String errorMessage = "해당 사용자는 존재하지 않습니다.";
            when(userService.getUser(anyLong())).thenThrow(new IllegalArgumentException(errorMessage));

            // When, Then
            mvc.perform(MockMvcRequestBuilders.get("/users/{id}", userId))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("회원 탈퇴 요청 처리")
    @WithMockUser
    class signout {
        private SignoutRequestDto signoutRequestDto;

        @BeforeEach
        void setUp() {
            User user = new User("username11", "nickname", "Aa123456789!", "test@sparta.com", "intro", UserStatus.IN_ACTION);
            signoutRequestDto = new SignoutRequestDto();
            signoutRequestDto.setPassword("Aa123456789!");

            UserDetailsImpl userDetails = Mockito.mock(UserDetailsImpl.class);
            Authentication authentication = Mockito.mock(Authentication.class);
            SecurityContext securityContext = Mockito.mock(SecurityContext.class);

            when(userDetails.getUser()).thenReturn(user);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            SecurityContextHolder.setContext(securityContext);
        }

        @Test
        @DisplayName("회원 탈퇴 성공")
        void SignoutSuccess() throws Exception {
            // given
            when(userService.signout(anyString(), any(SignoutRequestDto.class)))
                    .thenReturn(true);
            // when
            ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.patch("/users/signout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signoutRequestDto)));
            // then
            resultActions.andExpect(status().isOk())
                    .andExpect(content().string("탈퇴 성공"));
        }

        @Test
        @DisplayName("회원 탈퇴 실패")
        void SignoutFailure() throws Exception {
            // given
            when(userService.signout(anyString(), any(SignoutRequestDto.class)))
                    .thenReturn(false);
            // when
            ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.patch("/users/signout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signoutRequestDto)));

            // then
            resultActions.andExpect(status().isOk())
                    .andExpect(content().string("탈퇴 실패"));
        }
    }


    @Nested
    @DisplayName("회원 탈퇴 요청 처리")
    class updateUser {

        private UserUpdateRequest userUpdateRequest;
        private User user;

        @BeforeEach
        void setUp() {
            user = new User("username11", "nickname", "Aa123456789!", "test@sparta.com", "intro", UserStatus.IN_ACTION);
            userUpdateRequest = new UserUpdateRequest();
            userUpdateRequest.setIntro("edit-intro");
            userUpdateRequest.setNickname("edit-nickname");
            userUpdateRequest.setCurrentPassword("Aa123456789!");
            userUpdateRequest.setNewPassword("Ab123456789!!");
        }

        @Test
        void updateUserSuccess() throws Exception {
            long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.put("/users/{id}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userUpdateRequest)))
                    .andExpect(status().isOk())
                    .andReturn();
        }

        @Test
        @DisplayName("유저 업데이트 실패 - 비밀번호 불일치")
        public void updateUserPasswordMismatch() throws Exception {
            // Given
            long userId = 999L;
            userUpdateRequest.setCurrentPassword("A123456789!");
            userUpdateRequest.setNewPassword("Ab123456789!!");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(any(CharSequence.class), anyString())).thenReturn(false);

            // When & Then
            // updateUser code 수정 필요
            MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.put("/users/{id}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userUpdateRequest)))
                    .andExpect(status().isOk())
                    .andReturn();
        }
    }
}



