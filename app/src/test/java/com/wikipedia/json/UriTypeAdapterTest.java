package com.wikipedia.json;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;
import com.wikipedia.dataclient.Service;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static com.wikipedia.json.GsonMarshaller.marshal;
import static com.wikipedia.json.GsonUnmarshaller.unmarshal;

@RunWith(ParameterizedRobolectricTestRunner.class) public class UriTypeAdapterTest {
    @Parameters(name = "{0}") public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {{DeferredParam.NULL}, {DeferredParam.STRING},
                {DeferredParam.OPAQUE}, {DeferredParam.HIERARCHICAL}});
    }

    @Nullable private final Uri uri;

    public UriTypeAdapterTest(@NonNull DeferredParam param) {
        this.uri = param.val();
    }

    @Test public void testWriteRead() {
        Uri result = GsonUnmarshaller.unmarshal(Uri.class, GsonMarshaller.marshal(uri));
        assertThat(result, is(uri));
    }

    // Namespace uses a roboelectric mocked class internally, SparseArray, which is unavailable at
    // static time; defer evaluation until TestRunner is executed
    private enum DeferredParam {
        NULL() {
            @Nullable @Override Uri val() {
                return null;
            }
        },
        STRING() {
            @Nullable @Override Uri val() {
                return Uri.parse(Service.WIKIPEDIA_URL);
            }
        },
        OPAQUE() {
            @Nullable @Override Uri val() {
                return Uri.fromParts("http", "mediawiki.org", null);
            }
        },
        HIERARCHICAL() {
            @Nullable @Override Uri val() {
                return Uri.EMPTY;
            }
        };

        @Nullable abstract Uri val();
    }
}
