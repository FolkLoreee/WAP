package com.example.wap;

import org.junit.Test;

import static org.junit.Assert.*;

public class AuthUtilsTest {
    @Test
    public void test_valid_email_regex(){
        String email = "hello@gmail.com";
        boolean isValid = AuthUtils.validateEmail(email);
        assertTrue(isValid);
    }

    @Test
    public void test_invalid_email_regex(){
        String[] emails = new String[]{"hello@com","hello.com","hello",""};
        for(String email:emails){
            boolean isValid = AuthUtils.validateEmail(email);
            assertFalse(isValid);
        }
    }

    @Test
    public void test_empty_input_validation(){
        assertFalse(AuthUtils.validateInputField("","email"));
    }
    @Test
    public void test_invalid_email_validation(){
        assertFalse(AuthUtils.validateInputField("hello@com","email"));
    }
    @Test
    public void test_short_password_input_validation(){
        assertFalse(AuthUtils.validateInputField("abcde","password"));

    }
    @Test
    public void test_successful_input_validation_non_password() {
        assertTrue(AuthUtils.validateInputField("hello@outlook.com","email"));
    }
    @Test
    public void test_successful_input_validation_password() {
        assertTrue(AuthUtils.validateInputField("password1234","password"));
    }

}
