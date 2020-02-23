package com.wikipedia.json;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import com.wikipedia.dataclient.WikiSite;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static com.wikipedia.json.GsonMarshaller.marshal;
import static com.wikipedia.json.GsonUnmarshaller.unmarshal;

@RunWith(RobolectricTestRunner.class) public class WikiSiteTypeAdapterTest {
    @Test public void testWriteRead() {
        WikiSite wiki = WikiSite.forLanguageCode("test");
        assertThat(unmarshal(WikiSite.class, GsonMarshaller.marshal(wiki)), is(wiki));
    }

    @Test public void testReadNull() {
        assertThat(unmarshal(WikiSite.class, null), nullValue());
    }

    @Test public void testReadLegacyString() {
        String json = "\"https://test.wikipedia.org\"";
        WikiSite expected = WikiSite.forLanguageCode("test");
        assertThat(unmarshal(WikiSite.class, json), is(expected));
    }

    @Test public void testReadLegacyUri() {
        String json = "{\"domain\": \"test.wikipedia.org\", \"languageCode\": \"test\"}";
        WikiSite expected = WikiSite.forLanguageCode("test");
        assertThat(unmarshal(WikiSite.class, json), is(expected));
    }

    @Test public void testReadLegacyUriLang() {
        String json = "{\"domain\": \"test.wikipedia.org\"}";
        WikiSite expected = WikiSite.forLanguageCode("test");
        assertThat(unmarshal(WikiSite.class, json), is(expected));
    }

    @Test public void testReadLegacyLang() {
        String json = "{\"domain\": \"https://test.wikipedia.org\"}";
        WikiSite expected = WikiSite.forLanguageCode("test");
        assertThat(unmarshal(WikiSite.class, json), is(expected));
    }

}
