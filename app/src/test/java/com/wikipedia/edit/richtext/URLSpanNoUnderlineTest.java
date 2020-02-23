package com.wikipedia.edit.richtext;

import android.os.Parcelable;

import com.wikipedia.test.TestParcelUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import com.wikipedia.richtext.URLSpanNoUnderline;
import com.wikipedia.test.TestParcelUtil;

@RunWith(RobolectricTestRunner.class) public class URLSpanNoUnderlineTest {
    @Test public void testCtorParcel() throws Throwable {
        Parcelable subject = new URLSpanNoUnderline("url");
        TestParcelUtil.test(subject);
    }
}
