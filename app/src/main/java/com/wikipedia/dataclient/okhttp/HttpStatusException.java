package com.wikipedia.dataclient.okhttp;

import com.wikipedia.dataclient.restbase.RbServiceError;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wikipedia.dataclient.ServiceError;
import com.wikipedia.dataclient.restbase.RbServiceError;
import com.wikipedia.util.log.L;

import java.io.IOException;

import okhttp3.Response;

public class HttpStatusException extends IOException {
    private final int code;
    private final String url;
    @Nullable private ServiceError serviceError;

    public HttpStatusException(@NonNull Response rsp) {
        this.code = rsp.code();
        this.url = rsp.request().url().uri().toString();
        try {
            if (rsp.body() != null && rsp.body().contentType() != null
                    && rsp.body().contentType().toString().contains("json")) {
                serviceError = RbServiceError.create(rsp.body().string());
            }
        } catch (Exception e) {
            L.e(e);
        }
    }

    public HttpStatusException(@Nullable ServiceError error) {
        serviceError = error;
        code = 0;
        url = "";
    }

    public int code() {
        return code;
    }

    public ServiceError serviceError() {
        return serviceError;
    }

    @Override
    public String getMessage() {
        String str = "Code: " + code + ", URL: " + url;
        if (serviceError != null) {
            str += ", title: " + serviceError.getTitle() + ", detail: " + serviceError.getDetails();
        }
        return str;
    }
}
