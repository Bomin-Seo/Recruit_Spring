package com.sparta.icy.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SignupRequestDtoTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidSignupRequestDto() {
        SignupRequestDto dto = new SignupRequestDto();
        dto.setUsername("validUser123");
        dto.setPassword("Validpassword1@");
        dto.setNickname("validNickname");
        dto.setIntro("Valid intro");
        dto.setEmail("valid@example.com");
        dto.setStatus(true);

        Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testInvalidSignupRequestDto() {
        SignupRequestDto dto = new SignupRequestDto();
        dto.setUsername("user");
        dto.setPassword("invalid");
        dto.setNickname("");
        dto.setIntro("");
        dto.setEmail("invalid-email");

        Set<ConstraintViolation<SignupRequestDto>> violations = validator.validate(dto);
        assertEquals(6, violations.size());
    }
}