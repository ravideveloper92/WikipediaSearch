package com.wikipedia.espresso.page;

import android.Manifest;

import com.wikipedia.espresso.search.SearchBehaviors;
import com.wikipedia.espresso.util.CompareTools;
import com.wikipedia.espresso.util.InstrumentationViewUtils;
import com.wikipedia.espresso.util.ScreenshotTools;
import com.wikipedia.espresso.util.ViewTools;
import com.wikipedia.page.PageActivity;

import androidx.test.espresso.DataInteraction;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.core.AllOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.wikipedia.R;
import com.wikipedia.espresso.search.SearchBehaviors;
import com.wikipedia.espresso.util.ScreenshotTools;
import com.wikipedia.page.PageActivity;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.core.AllOf.allOf;
import static com.wikipedia.espresso.util.CompareTools.assertScreenshotWithinTolerance;
import static com.wikipedia.espresso.util.InstrumentationViewUtils.switchPageModeToBlack;
import static com.wikipedia.espresso.util.InstrumentationViewUtils.switchPageModeToDark;
import static com.wikipedia.espresso.util.ViewTools.WAIT_FOR_1000;
import static com.wikipedia.espresso.util.ViewTools.WAIT_FOR_2000;
import static com.wikipedia.espresso.util.ViewTools.WAIT_FOR_3000;
import static com.wikipedia.espresso.util.ViewTools.childAtPosition;
import static com.wikipedia.espresso.util.ViewTools.viewIsDisplayed;
import static com.wikipedia.espresso.util.ViewTools.waitFor;
import static com.wikipedia.espresso.util.ViewTools.whileWithMaxSteps;

@RunWith(AndroidJUnit4.class)
public final class PageActivityTest {

    @Rule
    public ActivityTestRule<PageActivity> activityTestRule = new ActivityTestRule<>(PageActivity.class);

    @Rule
    public GrantPermissionRule runtimePermissionRule = GrantPermissionRule.grant(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void testArticleLoad() throws Exception {

        testPage();
        InstrumentationViewUtils.switchPageModeToDark();
        ViewTools.waitFor(ViewTools.WAIT_FOR_1000);
        ScreenshotTools.snap("PageActivityWithObama_Dark");
        InstrumentationViewUtils.switchPageModeToBlack();
        ViewTools.waitFor(ViewTools.WAIT_FOR_1000);
        ScreenshotTools.snap("PageActivityWithObama_Black");


        //Todo: Create espresso.screenshots to show hide/show of tab layout and actionBar
        runComparisons();
    }

    private void testPage() {
        SearchBehaviors.searchKeywordAndGo("Barack Obama", true);

        ViewTools.whileWithMaxSteps(
                () -> !ViewTools.viewIsDisplayed(R.id.search_results_list),
                () -> ViewTools.waitFor(ViewTools.WAIT_FOR_1000));

        DataInteraction view = onData(anything())
                .inAdapterView(AllOf.allOf(withId(R.id.search_results_list),
                        ViewTools.childAtPosition(
                                withId(R.id.search_results_container),
                                1)))
                .atPosition(0);
        view.perform(click());

        ViewTools.whileWithMaxSteps(
                () -> !ViewTools.viewIsDisplayed(R.id.view_page_header_image),
                () -> ViewTools.waitFor(ViewTools.WAIT_FOR_2000));

        ViewTools.waitFor(ViewTools.WAIT_FOR_3000);
        ScreenshotTools.snap("PageActivityWithObama");

    }

    private void runComparisons() throws Exception {
        CompareTools.assertScreenshotWithinTolerance("PageActivityWithObama");
        CompareTools.assertScreenshotWithinTolerance("PageActivityWithObama_Dark");
        CompareTools.assertScreenshotWithinTolerance("PageActivityWithObama_Black");
        CompareTools.assertScreenshotWithinTolerance("SearchSuggestionPage");
        CompareTools.assertScreenshotWithinTolerance("SearchPage");
    }
}
