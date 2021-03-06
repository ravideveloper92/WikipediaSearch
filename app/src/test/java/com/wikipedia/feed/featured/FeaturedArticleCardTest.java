package com.wikipedia.feed.featured;

import com.wikipedia.json.GsonUnmarshaller;
import com.wikipedia.test.TestFileUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import com.wikipedia.dataclient.WikiSite;
import com.wikipedia.dataclient.page.PageSummary;
import com.wikipedia.json.GsonUnmarshaller;
import com.wikipedia.test.TestFileUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@RunWith(RobolectricTestRunner.class)
public class FeaturedArticleCardTest {
    private static WikiSite TEST = WikiSite.forLanguageCode("test");
    private PageSummary content;

    @Before public void setUp() throws Throwable {
        String json = TestFileUtil.readRawFile("featured_2016_11_07.json");
        content = GsonUnmarshaller.unmarshal(PageSummary.class, json);
    }

    @Test public void testTitleNormalization() {
        FeaturedArticleCard tfaCard = new FeaturedArticleCard(content, 0, TEST);
        assertThat(tfaCard.title(), not(containsString("_")));
    }
}
