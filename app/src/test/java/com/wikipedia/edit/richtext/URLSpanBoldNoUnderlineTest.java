package com.wikipedia.edit.richtext;

import android.os.Parcelable;

import com.wikipedia.test.TestParcelUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import com.wikipedia.richtext.URLSpanBoldNoUnderline;
import com.wikipedia.test.TestParcelUtil;

@RunWith(RobolectricTestRunner.class) public class URLSpanBoldNoUnderlineTest {
    @Test public void testCtorParcel() throws Throwable {
        Parcelable subject = new URLSpanBoldNoUnderline("url");
        TestParcelUtil.test(subject);
    }
}
