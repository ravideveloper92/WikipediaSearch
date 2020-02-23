package com.wikipedia.descriptions;

import android.content.Context;
import android.content.Intent;

import com.wikipedia.Constants;

import androidx.annotation.NonNull;

import com.wikipedia.Constants;
import com.wikipedia.activity.SingleFragmentActivityTransparent;

import static com.wikipedia.Constants.INTENT_EXTRA_INVOKE_SOURCE;

public class DescriptionEditSuccessActivity
        extends SingleFragmentActivityTransparent<DescriptionEditSuccessFragment>
        implements DescriptionEditSuccessFragment.Callback {

    static Intent newIntent(@NonNull Context context, @NonNull Constants.InvokeSource invokeSource) {
        return new Intent(context, DescriptionEditSuccessActivity.class)
                .putExtra(Constants.INTENT_EXTRA_INVOKE_SOURCE, invokeSource);
    }

    @Override protected DescriptionEditSuccessFragment createFragment() {
        return DescriptionEditSuccessFragment.newInstance();
    }

    @Override
    public void onDismissClick() {
        setResult(RESULT_OK, getIntent());
        finish();
    }
}
