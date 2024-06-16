package com.sparta.icy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.icy.IcyApplication;
import com.sparta.icy.dto.*;
import com.sparta.icy.entity.User;
import com.sparta.icy.entity.UserStatus;
import com.sparta.icy.exception.AlreadySignedOutUserCannotBeSignoutAgainException;
import com.sparta.icy.exception.InvalidPasswordException;
import com.sparta.icy.jwt.JwtUtil;
import com.sparta.icy.repository.UserRepository;
import com.sparta.icy.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ContextConfiguration(classes = IcyApplication.class)
@ActiveProfiles("test")
public class UserServiceTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockBean
    private JwtUtil jwtUtil;
    @Autowired
    private LogService logService;
    @Autowired
    private UserService userService;

    User user;
    @BeforeEach
    void setUp() {
        user = new User("username11", "nickname", "Aa123456789!", "test@example.com", "intro", UserStatus.IN_ACTION);
    }

    @Nested
    @DisplayName("회원 가입 테스트")
    class signup {

        private SignupRequestDto signupRequestDto;

        @BeforeEach
        void setUp() {
            signupRequestDto = new SignupRequestDto();
            signupRequestDto.setUsername("username11");
            signupRequestDto.setPassword("Aa123456789!");
            signupRequestDto.setNickname("nickname");
            signupRequestDto.setEmail("test@example.com");
            signupRequestDto.setIntro("intro");
        }

        @Test
        @Order(1)
        @DisplayName("회원 가입 성공")
        void SignupSuccess() throws Exception {

            // When
            userService.signup(signupRequestDto);
            // Then
            Optional<User> testUser = userRepository.findByUsername("username11");
            assertTrue(testUser.isPresent());
            assertEquals("username11", testUser.get().getUsername());
            assertEquals("nickname", testUser.get().getNickname());
            assertEquals("intro", testUser.get().getIntro());
        }

        @Test
        @Order(2)
        @DisplayName("회원 가입 실패")
        void SignupFail() throws Exception {
            userRepository.save(user);
            assertThrows(AlreadySignedOutUserCannotBeSignoutAgainException.class, () -> userService.signup(signupRequestDto));
        }
    }
    @Nested
    @DisplayName("프로필 조회")
    class getUser {
        @Test
        @DisplayName("사용자 프로필 조회 - 성공")
        void getUserSuccess() {
            // Given
            userRepository.save(user);
            // When
            UserProfileResponse userProfile = userService.getUser(user.getId());
            // Then
            assertNotNull(userProfile);
            assertEquals("username11", userProfile.getUsername());
            assertEquals("nickname", userProfile.getNickname());
        }

        @Test
        @DisplayName("사용자 프로필 조회 - 사용자 없음")
        void getUserFail() throws Exception {
            assertThrows(IllegalArgumentException.class, () -> userService.getUser(999L));
        }


    }

//    @Nested
//    @DisplayName("프로필 수정")
//    class updateUser {
//        UserUpdateRequest updateRequest;
//        LoginRequestDto loginRequestDto;
//        HttpServletResponse res;
//        SignupRequestDto signupRequestDto;
//
//        @BeforeEach
//        void setUp() {
//            signupRequestDto = new SignupRequestDto();
//            signupRequestDto.setUsername("username12");
//            signupRequestDto.setPassword("Aa123456789!");
//            signupRequestDto.setNickname("nickname");
//            signupRequestDto.setEmail("test@example.com");
//            signupRequestDto.setIntro("intro");
//
//            userService.signup(signupRequestDto);
//
//            updateRequest = new UserUpdateRequest();
//            updateRequest.setCurrentPassword("Aa123456789!");
//            updateRequest.setNewPassword("aB123456789!");
//            updateRequest.setNickname("newNickname");
//
//            loginRequestDto = new LoginRequestDto("username12", "Aa123456789!");
//            logService.login(loginRequestDto, res);
//        }
//
//        @Test
//        @DisplayName("사용자 정보 수정 - 성공")
//        void updateUser_Success() {
//            Optional<User> testUser = userRepository.findByUsername("username12");
//
//            User updatedUser = userService.updateUser(testUser.get().getId(), updateRequest);
//            // Then
//            assertEquals("newNickname", updatedUser.getNickname());
//            assertTrue(passwordEncoder.matches("aB123456789!", updatedUser.getPassword()));
//        }
//
//        @Test
//        @DisplayName("사용자 정보 수정 - 잘못된 비밀번호")
//        void updateUser_WrongPassword() {
//
//            UserUpdateRequest updateRequest = new UserUpdateRequest();
//            updateRequest.setCurrentPassword("wrongPassword");
//            // When / Then
//            assertThrows(InvalidPasswordException.class, () -> userService.updateUser(user.getId(), updateRequest));
//        }
//
//        @Test
//        @DisplayName("사용자 정보 수정 - 현재 비밀번호와 동일한 비밀번호로 수정 불가")
//        void updateUser_SamePassword() {
//            // Given
//            User existingUser = new User("testuser", "nickname", passwordEncoder.encode("password"), "test@example.com", "intro", UserStatus.IN_ACTION);
//            userRepository.save(existingUser);
//            UserUpdateRequest updateRequest = new UserUpdateRequest();
//            updateRequest.setCurrentPassword("password");
//            updateRequest.setNewPassword("password");
//
//            UserDetailsImpl userDetails = new UserDetailsImpl(existingUser);
//            Authentication authentication = mock(Authentication.class);
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//            when(authentication.getPrincipal()).thenReturn(userDetails);
//
//            // When / Then
//            assertThrows(IllegalArgumentException.class, () -> userService.updateUser(existingUser.getId(), updateRequest));
//        }
//
//    }
//        @Nested
//        @DisplayName("사용자 탈퇴")
//        class logout {
//
//            private SignoutRequestDto signoutRequestDto;
//
//            @BeforeEach
//            void setUp() {
//                userRepository.save(user);
//                signoutRequestDto = new SignoutRequestDto();
//                signoutRequestDto.setPassword("Aa123456789!");
//
//                UserDetailsImpl userDetails = new UserDetailsImpl(user);
//                Authentication authentication = mock(Authentication.class);
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//                when(authentication.getPrincipal()).thenReturn(userDetails);
//            }
//
//            @Test
//            @DisplayName("사용자 탈퇴 - 성공")
//            void signout_Success() {
//
//                // When
//                boolean result = userService.signout(user.getUsername(), signoutRequestDto);
//                // Then
//                assertTrue(result);
//                assertEquals(UserStatus.SECESSION.getStatus(), userRepository.findByUsername("username11").get().getStatus());
//            }
//
//            @Test
//            @DisplayName("사용자 탈퇴 - 잘못된 비밀번호")
//            void signout_WrongPassword() {
//                // Given
//                User existingUser = new User("testuser", "nickname", passwordEncoder.encode("password"), "test@example.com", "intro", UserStatus.IN_ACTION);
//                userRepository.save(existingUser);
//                SignoutRequestDto signoutRequestDto = new SignoutRequestDto();
//                signoutRequestDto.setPassword("wrongPassword");
//
//                UserDetailsImpl userDetails = new UserDetailsImpl(existingUser);
//                Authentication authentication = mock(Authentication.class);
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//                when(authentication.getPrincipal()).thenReturn(userDetails);
//
//                // When
//                boolean result = userService.signout(existingUser.getUsername(), signoutRequestDto);
//
//                // Then
//                assertFalse(result);
//                assertEquals(UserStatus.IN_ACTION.getStatus(), userRepository.findByUsername("testuser").get().getStatus());
//            }
//
//            @Test
//            @DisplayName("사용자 탈퇴 - 이미 탈퇴한 사용자")
//            void signout_AlreadySignedOut() {
//                // Given
//                User existingUser = new User("testuser", "nickname", passwordEncoder.encode("password"), "test@example.com", "intro", UserStatus.SECESSION);
//                userRepository.save(existingUser);
//                SignoutRequestDto signoutRequestDto = new SignoutRequestDto();
//                signoutRequestDto.setPassword("password");
//
//                UserDetailsImpl userDetails = new UserDetailsImpl(existingUser);
//                Authentication authentication = mock(Authentication.class);
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//                when(authentication.getPrincipal()).thenReturn(userDetails);
//
//                // When
//                boolean result = userService.signout(existingUser.getUsername(), signoutRequestDto);
//
//                // Then
//                assertFalse(result);
//                assertEquals(UserStatus.SECESSION.getStatus(), userRepository.findByUsername("testuser").get().getStatus());
//            }
//
//        }
}




