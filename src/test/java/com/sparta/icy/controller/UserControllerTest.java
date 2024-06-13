package com.sparta.icy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.icy.config.WebSecurityConfig;
import com.sparta.icy.dto.SignoutRequestDto;
import com.sparta.icy.dto.SignupRequestDto;
import com.sparta.icy.entity.User;
import com.sparta.icy.entity.UserStatus;
import com.sparta.icy.security.UserDetailsImpl;
import com.sparta.icy.service.LogService;
import com.sparta.icy.service.UserService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.patch;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = {UserController.class, LogController.class},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = WebSecurityConfig.class
                )
        }
)

class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    private Principal mockPrincipal;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    @MockBean
    LogService logService;

    private void mockUserSetup() {
        // Mock 테스트 유져 생성
        String username = "username11";
        String nickname = "nickname";
        String password = "Aa123456789!";
        String email = "test@sparta.com";
        String intro = "hi";
        User testUser = new User(username, nickname, password, email, intro, UserStatus.IN_ACTION);
        UserDetailsImpl testUserDetails = new UserDetailsImpl(testUser);
        mockPrincipal = new UsernamePasswordAuthenticationToken(testUserDetails, "", testUserDetails.getAuthorities());
    }

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity(new MockSpringSecurityFilter()))
                .build();
    }

    @Test
    @DisplayName("로그인 Page")
    void login() throws Exception {
        // when - then
        mvc.perform(post("/logs/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andDo(print());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getUser() {
    }

    @Nested
    @DisplayName("회원 가입 요청 처리")
    class signup {

        @Test
        @DisplayName("회원 가입 성공 Case")
        void signup_success() throws Exception{
            // given
            SignupRequestDto signupRequestDto = new SignupRequestDto();
            signupRequestDto.setUsername("username11");
            signupRequestDto.setNickname("nickname");
            signupRequestDto.setPassword("Aa123456789!");
            signupRequestDto.setEmail("test@sparta.com");
            signupRequestDto.setIntro("hi");

            // when
            ResultActions resultActions = mvc.perform(post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequestDto)));

            // then
            resultActions.andExpect(status().isOk())
                    .andExpect(content().string("회원가입 성공"));

            assertEquals("회원가입 성공", resultActions.andReturn().getResponse().getContentAsString(),
                    () -> "비밀번호 작성 조건 혹은 빈 필드가 없는지 확인해주세요.");
        }
        @Test
        @DisplayName("회원 가입 실패 - 비밀번호 입력 조건 미준수")
        void signup_fail() throws Exception{
            SignupRequestDto signupRequestDto = new SignupRequestDto();
            signupRequestDto.setUsername("username11");
            signupRequestDto.setNickname("nickname");
            signupRequestDto.setPassword("A123456789");
            signupRequestDto.setEmail("test@sparta.com");
            signupRequestDto.setIntro("hi");

            ResultActions resultActions = mvc.perform(post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequestDto)));

            // then
            resultActions.andExpect(status().isOk())
                    .andExpect(content().string("회원 가입 실패"));

        }

        @Test
        @DisplayName("회원 가입 실패 - 입력되지 않은 field 존재")
        void signup_fail2() throws Exception{
            SignupRequestDto signupRequestDto = new SignupRequestDto();
            signupRequestDto.setUsername("username11");
            signupRequestDto.setNickname("nickname");
            signupRequestDto.setPassword("A123456789");
            signupRequestDto.setEmail("");
            signupRequestDto.setIntro("");

            ResultActions resultActions = mvc.perform(post("/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequestDto)));

            // then
            resultActions.andExpect(status().isOk())
                    .andExpect(content().string("회원 가입 실패"));
        }
    }


//    @Test
//    void signout() throws Exception{
//        // given
//        SignoutRequestDto signoutRequestDto = new SignoutRequestDto();
//        signoutRequestDto.setPassword("사용자 요청");
//
//        // Mocking userService의 signout 메서드가 성공적으로 처리되도록 설정
//        Mockito.when(userService.signout(Mockito.anyString(), Mockito.any(SignoutRequestDto.class)))
//                .thenReturn(true);
//
//        // when
//        ResultActions resultActions = mvc.perform(patch("/users/signout")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(signoutRequestDto)));
//
//        // then
//        resultActions.andExpect(status().isOk())
//                .andExpect(content().string("탈퇴 성공"));
//    }

    @Test
    void updateUser() {
    }


//    @Test
//    @DisplayName("신규 관심상품 등록")
//    void test3() throws Exception {
//        // given
//        this.mockUserSetup();
//        String title = "Apple <b>아이폰</b> 14 프로 256GB [자급제]";
//        String imageUrl = "https://shopping-phinf.pstatic.net/main_3456175/34561756621.20220929142551.jpg";
//        String linkUrl = "https://search.shopping.naver.com/gate.nhn?id=34561756621";
//        int lPrice = 959000;
//        ProductRequestDto requestDto = new ProductRequestDto(
//                title,
//                imageUrl,
//                linkUrl,
//                lPrice
//        );
//
//        String postInfo = objectMapper.writeValueAsString(requestDto);
//
//        // when - then
//        mvc.perform(post("/api/products")
//                        .content(postInfo)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON)
//                        .principal(mockPrincipal)
//                )
//                .andExpect(status().isOk())
//                .andDo(print());
//    }
}



