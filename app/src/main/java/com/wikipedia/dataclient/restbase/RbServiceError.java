package com.wikipedia.dataclient.restbase;

import com.wikipedia.json.GsonUnmarshaller;

import androidx.annotation.NonNull;

import org.apache.commons.lang3.StringUtils;
import com.wikipedia.dataclient.ServiceError;
import com.wikipedia.json.GsonUnmarshaller;
import com.wikipedia.model.BaseModel;

/**
 * Gson POJO for a RESTBase API error.
 */
public class RbServiceError extends BaseModel implements ServiceError {
    @SuppressWarnings("unused") private String type;
    @SuppressWarnings("unused") private String title;
    @SuppressWarnings("unused") private String detail;
    @SuppressWarnings("unused") private String method;
    @SuppressWarnings("unused") private String uri;

    public static RbServiceError create(@NonNull String rspBody) {
        return GsonUnmarshaller.unmarshal(RbServiceError.class, rspBody);
    }

    @Override
    @NonNull
    public String getTitle() {
        return StringUtils.defaultString(title);
    }

    @Override
    @NonNull
    public String getDetails() {
        return StringUtils.defaultString(detail);
    }
}
