package com.wikipedia.page;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import com.wikipedia.dataclient.WikiSite;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static com.wikipedia.page.Namespace.FILE;
import static com.wikipedia.page.Namespace.MAIN;
import static com.wikipedia.page.Namespace.MEDIA;
import static com.wikipedia.page.Namespace.SPECIAL;
import static com.wikipedia.page.Namespace.TALK;

@RunWith(RobolectricTestRunner.class) public class NamespaceTest {
    private static Locale PREV_DEFAULT_LOCALE;

    @BeforeClass public static void setUp() {
        PREV_DEFAULT_LOCALE = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @AfterClass public static void tearDown() {
        Locale.setDefault(PREV_DEFAULT_LOCALE);
    }

    @Test public void testOf() {
        MatcherAssert.assertThat(Namespace.of(Namespace.SPECIAL.code()), Matchers.is(Namespace.SPECIAL));
    }

    @Test public void testFromLegacyStringMain() {
        //noinspection deprecation
        MatcherAssert.assertThat(Namespace.fromLegacyString(WikiSite.forLanguageCode("test"), null), Matchers.is(Namespace.MAIN));
    }

    @Test public void testFromLegacyStringFile() {
        //noinspection deprecation
        MatcherAssert.assertThat(Namespace.fromLegacyString(WikiSite.forLanguageCode("he"), "קובץ"), Matchers.is(Namespace.FILE));
    }

    @Test public void testFromLegacyStringSpecial() {
        //noinspection deprecation
        MatcherAssert.assertThat(Namespace.fromLegacyString(WikiSite.forLanguageCode("lez"), "Служебная"), Matchers.is(Namespace.SPECIAL));
    }

    @Test public void testFromLegacyStringTalk() {
        //noinspection deprecation
        MatcherAssert.assertThat(Namespace.fromLegacyString(WikiSite.forLanguageCode("en"), "stringTalk"), Matchers.is(Namespace.TALK));
    }

    @Test public void testCode() {
        MatcherAssert.assertThat(Namespace.MAIN.code(), is(0));
        MatcherAssert.assertThat(Namespace.TALK.code(), is(1));
    }

    @Test public void testSpecial() {
        MatcherAssert.assertThat(Namespace.SPECIAL.special(), is(true));
        MatcherAssert.assertThat(Namespace.MAIN.special(), is(false));
    }

    @Test public void testMain() {
        MatcherAssert.assertThat(Namespace.MAIN.main(), is(true));
        MatcherAssert.assertThat(Namespace.TALK.main(), is(false));
    }

    @Test public void testFile() {
        MatcherAssert.assertThat(Namespace.FILE.file(), is(true));
        MatcherAssert.assertThat(Namespace.MAIN.file(), is(false));
    }

    @Test public void testTalkNegative() {
        MatcherAssert.assertThat(Namespace.MEDIA.talk(), is(false));
        MatcherAssert.assertThat(Namespace.SPECIAL.talk(), is(false));
    }

    @Test public void testTalkZero() {
        MatcherAssert.assertThat(Namespace.MAIN.talk(), is(false));
    }

    @Test public void testTalkOdd() {
        MatcherAssert.assertThat(Namespace.TALK.talk(), is(true));
    }

    @Test public void testToLegacyStringMain() {
        //noinspection deprecation
        MatcherAssert.assertThat(Namespace.MAIN.toLegacyString(), nullValue());
    }

    @Test public void testToLegacyStringNonMain() {
        //noinspection deprecation
        MatcherAssert.assertThat(Namespace.TALK.toLegacyString(), is("Talk"));
    }
}
