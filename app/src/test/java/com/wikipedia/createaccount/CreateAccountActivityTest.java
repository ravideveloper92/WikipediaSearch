package com.wikipedia.createaccount;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static com.wikipedia.createaccount.CreateAccountActivity.ValidateResult;
import static com.wikipedia.createaccount.CreateAccountActivity.validateInput;

@RunWith(RobolectricTestRunner.class) public class CreateAccountActivityTest {

    @Test public void testValidateInputSuccessWithEmail() {
        MatcherAssert.assertThat(CreateAccountActivity.validateInput("user", "password", "password", "test@example.com"),
                Matchers.is(CreateAccountActivity.ValidateResult.SUCCESS));
    }
    @Test public void testValidateInvalidEmail() {
        MatcherAssert.assertThat(CreateAccountActivity.validateInput("user", "password", "password", ""),
                Matchers.is(CreateAccountActivity.ValidateResult.NO_EMAIL));
    }
    @Test public void testValidateInputInvalidUser() {
        MatcherAssert.assertThat(CreateAccountActivity.validateInput("user[]", "password", "password", ""),
                Matchers.is(CreateAccountActivity.ValidateResult.INVALID_USERNAME));
    }

    @Test public void testValidateInputInvalidPassword() {
        MatcherAssert.assertThat(CreateAccountActivity.validateInput("user", "foo", "password", ""),
                Matchers.is(CreateAccountActivity.ValidateResult.INVALID_PASSWORD));
    }

    @Test public void testValidateInputPasswordMismatch() {
        MatcherAssert.assertThat(CreateAccountActivity.validateInput("user", "password", "passw0rd", ""),
                Matchers.is(CreateAccountActivity.ValidateResult.PASSWORD_MISMATCH));
    }

    @Test public void testValidateInputInvalidEmail() {
        MatcherAssert.assertThat(CreateAccountActivity.validateInput("user", "password", "password", "foo"),
                Matchers.is(CreateAccountActivity.ValidateResult.INVALID_EMAIL));
    }
}
