package com.wikipedia.edit.richtext;

import android.os.Parcelable;

import com.wikipedia.test.TestParcelUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import com.wikipedia.test.TestParcelUtil;

@RunWith(RobolectricTestRunner.class) public class ColorSpanExTest {
    @Test public void testCtorParcel() throws Throwable {
        SyntaxRule rule = new SyntaxRule("startSymbol", "endSymbol", SyntaxRuleStyle.BOLD);
        Parcelable subject = new ColorSpanEx(1, 2, 10, rule);
        TestParcelUtil.test(subject);
    }
}
