package com.wikipedia.login;

import com.wikipedia.dataclient.WikiSite;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wikipedia.dataclient.WikiSite;

class LoginResetPasswordResult extends LoginResult {
    LoginResetPasswordResult(@NonNull WikiSite site, @NonNull String status, @Nullable String userName,
                             @Nullable String password, @Nullable String message) {
        super(site, status, userName, password, message);
    }
}
