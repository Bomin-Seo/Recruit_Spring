package com.sparta.icy.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SignoutRequestDtoTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidSignoutRequestDto() {
        SignoutRequestDto dto = new SignoutRequestDto();
        dto.setPassword("Validpassword1@");

        Set<ConstraintViolation<SignoutRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testInvalidSignoutRequestDto() {
        SignoutRequestDto dto = new SignoutRequestDto();
        dto.setPassword("invalid");

        Set<ConstraintViolation<SignoutRequestDto>> violations = validator.validate(dto);
        assertEquals(2, violations.size());
    }
}