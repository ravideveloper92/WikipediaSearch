package com.wikipedia.savedpages;

import com.wikipedia.dataclient.page.PageLead;
import com.wikipedia.html.ImageTagParser;
import com.wikipedia.html.PixelDensityDescriptorParser;
import com.wikipedia.page.Section;

import androidx.annotation.NonNull;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import com.wikipedia.dataclient.page.PageLead;
import com.wikipedia.html.ImageTagParser;
import com.wikipedia.html.PixelDensityDescriptorParser;
import com.wikipedia.page.Section;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PageImageUrlParserTest {
    @NonNull private final PageImageUrlParser subject = new PageImageUrlParser(new ImageTagParser(),
            new PixelDensityDescriptorParser());

    @Test public void testParseLeadPronunciation() {
        PageLead lead = mock(PageLead.class);
        when(lead.getTitlePronunciationUrl()).thenReturn("url");
        when(lead.getLeadSectionContent()).thenReturn("");
        assertThat(subject.parse(lead, 0), contains("url"));
    }

    @Test public void testParseLeadLeadImage() {
        PageLead lead = mock(PageLead.class);
        when(lead.getLeadImageUrl(anyInt())).thenReturn("url");
        when(lead.getLeadSectionContent()).thenReturn("");
        assertThat(subject.parse(lead, 0), contains("url"));
    }

    @Test public void testParseLeadContentImages() {
        PageLead lead = mock(PageLead.class);
        when(lead.getLeadSectionContent()).thenReturn("<img src='url'>");
        assertThat(subject.parse(lead, 0), contains("url"));
    }

    @Test public void testParseLeadEmpty() {
        PageLead lead = mock(PageLead.class);
        when(lead.getLeadSectionContent()).thenReturn("");
        assertThat(subject.parse(lead, 0), empty());
    }

    @Test public void testParseSections() {
        Section section = mock(Section.class);
        when(section.getContent()).thenReturn("<img src='url'>");
        List<Section> sections = Collections.singletonList(section);
        MatcherAssert.assertThat(subject.parse(sections), contains("url"));
    }

    @Test public void testParseSectionsEmpty() {
        MatcherAssert.assertThat(subject.parse(Collections.<Section>emptyList()), empty());
    }

    @Test public void testParseHtml() {
        assertThat(subject.parse("<img src='url'>"), contains("url"));
    }

    @Test public void testParseHtmlEmpty() {
        assertThat(subject.parse(""), empty());
    }
}
