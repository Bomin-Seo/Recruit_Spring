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

    private void setupSecurityContext(User user_) {
        UserDetailsImpl userDetails = new UserDetailsImpl(user_);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
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

    @Nested
    @DisplayName("프로필 수정")
    class updateUser {
        UserUpdateRequest updateRequest;
        LoginRequestDto loginRequestDto;
        HttpServletResponse res;
        SignupRequestDto signupRequestDto;

        @BeforeEach
        void setUp() {
            signupRequestDto = new SignupRequestDto();
            signupRequestDto.setUsername("username12");
            signupRequestDto.setPassword("Aa123456789!");
            signupRequestDto.setNickname("nickname");
            signupRequestDto.setEmail("test@example.com");
            signupRequestDto.setIntro("intro");

            userService.signup(signupRequestDto);

            updateRequest = new UserUpdateRequest();
            updateRequest.setCurrentPassword("Aa123456789!");
            updateRequest.setNewPassword("aB123456789!");
            updateRequest.setNickname("newNickname");
            updateRequest.setIntro("new intro");

            User createdUser = userRepository.findByUsername("username12").orElseThrow();
            setupSecurityContext(createdUser);
        }

        @Test
        @DisplayName("사용자 정보 수정 - 성공")
        void updateUserSuccess() {
            Optional<User> testUser = userRepository.findByUsername("username12");
            User updatedUser = userService.updateUser(testUser.get().getId(), updateRequest);
            // Then

            assertEquals("newNickname", updatedUser.getNickname());
            assertTrue(passwordEncoder.matches("aB123456789!", updatedUser.getPassword()));
        }

        @Test
        @DisplayName("사용자 정보 수정 - 잘못된 비밀번호")
        void updateUserFailure() {
            // Given
            UserUpdateRequest wrongPasswordRequest = new UserUpdateRequest();
            wrongPasswordRequest.setCurrentPassword("wrongPassword");
            wrongPasswordRequest.setNewPassword("aB123456789!");
            wrongPasswordRequest.setNickname("newNickname");
            wrongPasswordRequest.setIntro("new intro");

            // When / Then
            Optional<User> testUser = userRepository.findByUsername("username12");
            assertThrows(InvalidPasswordException.class, () -> userService.updateUser(testUser.get().getId(), wrongPasswordRequest));
        }

        @Test
        @DisplayName("사용자 정보 수정 - 현재 비밀번호와 동일한 비밀번호로 수정 불가")
        void updateUserFailure2() {
            // Given
            UserUpdateRequest samePasswordRequest = new UserUpdateRequest();
            samePasswordRequest.setCurrentPassword("Aa123456789!");
            samePasswordRequest.setNewPassword("Aa123456789!");
            samePasswordRequest.setNickname("newNickname");
            samePasswordRequest.setIntro("new intro");

            // When / Then
            Optional<User> testUser = userRepository.findByUsername("username12");
            assertThrows(IllegalArgumentException.class, () -> userService.updateUser(testUser.get().getId(), samePasswordRequest));
        }

    }
        @Nested
        @DisplayName("사용자 탈퇴")
        class logout {
            private SignupRequestDto signupRequestDto;

            @BeforeEach
            void setUp() {
                signupRequestDto = new SignupRequestDto();
                signupRequestDto.setUsername("username13");
                signupRequestDto.setPassword("Aa123456789!");
                signupRequestDto.setNickname("nickname");
                signupRequestDto.setEmail("test@example.com");
                signupRequestDto.setIntro("intro");

                userService.signup(signupRequestDto);

                User createdUser = userRepository.findByUsername("username13").orElseThrow();
                setupSecurityContext(createdUser);
            }

            @Test
            @DisplayName("사용자 탈퇴 - 성공")
            void signoutSuccess() {
                SignoutRequestDto signoutRequestDto = new SignoutRequestDto();
                signoutRequestDto.setPassword("Aa123456789!");
                // When
                boolean result = userService.signout("username13", signoutRequestDto);
                // Then
                assertTrue(result);
                Optional<User> signedOutUser = userRepository.findByUsername("username13");
                assertTrue(signedOutUser.isPresent());
                assertEquals(UserStatus.SECESSION.getStatus(), signedOutUser.get().getStatus());
            }

            @Test
            @DisplayName("사용자 탈퇴 실패 - 잘못된 비밀번호")
            void signoutFailure() {
                SignoutRequestDto signoutRequestDto = new SignoutRequestDto();
                signoutRequestDto.setPassword("A");
                // When
                boolean result = userService.signout("username13", signoutRequestDto);
                // Then
                assertFalse(result);
            }

//            @Test
//            @DisplayName("사용자 탈퇴 - 이미 탈퇴한 사용자")
//            void signoutFailure2() {
//                User testUser = new User("username14", "nickname2", passwordEncoder.encode("Aa123456789!"), "test2@example.com", "intro2", UserStatus.SECESSION);
//                userRepository.save(testUser);
//
//                // Simulate user login
//                UserDetailsImpl userDetails = new UserDetailsImpl(testUser);
//                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//
//                // When
//                SignoutRequestDto signoutRequestDto = new SignoutRequestDto();
//                signoutRequestDto.setPassword("Aa123456789!");
//
//                boolean result = userService.signout(testUser.getUsername(), signoutRequestDto);
//
//                // Then
//                assertFalse(result);
//                assertEquals(UserStatus.SECESSION.getStatus(), testUser.getStatus());
//            }

        }
}




