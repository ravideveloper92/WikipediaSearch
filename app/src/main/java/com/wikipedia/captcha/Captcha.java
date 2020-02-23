package com.wikipedia.captcha;

import com.wikipedia.dataclient.mwapi.MwResponse;

import androidx.annotation.NonNull;

import com.wikipedia.dataclient.mwapi.MwResponse;

public class Captcha extends MwResponse {
    @SuppressWarnings("unused,NullableProblems") @NonNull private FancyCaptchaReload fancycaptchareload;
    @NonNull String captchaId() {
        return fancycaptchareload.index();
    }

    private static class FancyCaptchaReload {
        @SuppressWarnings("unused,NullableProblems") @NonNull private String index;
        @NonNull String index() {
            return index;
        }
    }
}
