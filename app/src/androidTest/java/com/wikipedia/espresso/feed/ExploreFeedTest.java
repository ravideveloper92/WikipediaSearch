package com.wikipedia.espresso.feed;

import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.text.TextUtils;

import com.wikipedia.espresso.util.CompareTools;
import com.wikipedia.espresso.util.InstrumentationViewUtils;
import com.wikipedia.espresso.util.ScreenshotTools;
import com.wikipedia.espresso.util.ViewTools;
import com.wikipedia.main.MainActivity;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.wikipedia.R;
import com.wikipedia.espresso.util.ScreenshotTools;
import com.wikipedia.espresso.util.ViewTools;
import com.wikipedia.main.MainActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.core.AllOf.allOf;
import static com.wikipedia.espresso.util.CompareTools.assertScreenshotWithinTolerance;
import static com.wikipedia.espresso.util.InstrumentationViewUtils.switchToBlackMode;
import static com.wikipedia.espresso.util.InstrumentationViewUtils.switchToDarkMode;
import static com.wikipedia.espresso.util.ViewTools.WAIT_FOR_2000;
import static com.wikipedia.espresso.util.ViewTools.rotateScreen;
import static com.wikipedia.espresso.util.ViewTools.setTextInTextView;
import static com.wikipedia.espresso.util.ViewTools.viewIsDisplayed;
import static com.wikipedia.espresso.util.ViewTools.waitFor;
import static com.wikipedia.espresso.util.ViewTools.whileWithMaxSteps;

@RunWith(AndroidJUnit4.class)
@SuppressWarnings("checkstyle:magicnumber")
public class ExploreFeedTest {

    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule runtimePermissionRule = GrantPermissionRule.grant(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void testExploreFeed() throws Exception {
        waitUntilFeedDisplayed();

        runTests("");

        InstrumentationViewUtils.switchToDarkMode();

        runTests("_Dark");

        InstrumentationViewUtils.switchToBlackMode();

        runTests("_Black");

        runComparisons();

    }

    private void runTests(String mode) {
        testCards("" + mode);

        waitUntilFeedDisplayed();

        ViewTools.rotateScreen(getActivity(), ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        ViewTools.waitFor(2000);
        testCards("_Landscape" + mode);
        ViewTools.rotateScreen(getActivity(), ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewTools.waitFor(2000);
    }

    private void runComparisons() throws Exception {
        CompareTools.assertScreenshotWithinTolerance("FeaturedArticle");
        CompareTools.assertScreenshotWithinTolerance("FeaturedArticle_Landscape");
        CompareTools.assertScreenshotWithinTolerance("FeaturedImage");
        CompareTools.assertScreenshotWithinTolerance("FeaturedImage_Landscape");
        CompareTools.assertScreenshotWithinTolerance("MainPage");
        CompareTools.assertScreenshotWithinTolerance("MainPage_Landscape");
        CompareTools.assertScreenshotWithinTolerance("News");
        CompareTools.assertScreenshotWithinTolerance("News_Landscape");
        CompareTools.assertScreenshotWithinTolerance("OnThisDay");
        CompareTools.assertScreenshotWithinTolerance("OnThisDay_Landscape");
        CompareTools.assertScreenshotWithinTolerance("Randomizer");
        CompareTools.assertScreenshotWithinTolerance("Randomizer_Landscape");
        CompareTools.assertScreenshotWithinTolerance("Trending");
        CompareTools.assertScreenshotWithinTolerance("Trending_Landscape");
    }

    private Activity getActivity() {
        return activityTestRule.getActivity();
    }

    private static void testCards(String postFix) {
        waitUntilFeedDisplayed();

        onView(withId(R.id.fragment_feed_feed)).perform(RecyclerViewActions.scrollToPosition(10));
        ViewTools.waitFor(1000);

        onView(withId(R.id.fragment_feed_feed)).perform(RecyclerViewActions.scrollToPosition(7));
        ViewTools.waitFor(1000);

        //Only condition where the 0th view doesn't have the date view, 1st has.
        if (TextUtils.isEmpty(postFix)) {
            ViewInteraction colorButton = onView(
                    allOf(
                            ViewTools.matchPosition(allOf(withId(R.id.view_card_header_subtitle)), 1),
                            isDisplayed()));
            colorButton.perform(ViewTools.setTextInTextView("Feb 5, 2017"));
        } else {
            setDate();
        }
        setDayText();
        ViewTools.waitFor(1000);
        ScreenshotTools.snap("FeaturedImage" + postFix);

        onView(withId(R.id.fragment_feed_feed)).perform(RecyclerViewActions.scrollToPosition(6));
        setDate();
        ViewTools.waitFor(1000);
        ScreenshotTools.snap("FeaturedArticle" + postFix);

        onView(withId(R.id.fragment_feed_feed)).perform(RecyclerViewActions.scrollToPosition(5));
        setDate();
        ViewTools.waitFor(1000);
        ScreenshotTools.snap("Randomizer" + postFix);

        onView(withId(R.id.fragment_feed_feed)).perform(RecyclerViewActions.scrollToPosition(4));
        setDate();
        setStaticCardDate();
        ViewTools.waitFor(1000);
        ScreenshotTools.snap("MainPage" + postFix);

        onView(withId(R.id.fragment_feed_feed)).perform(RecyclerViewActions.scrollToPosition(3));
        setDate();
        ViewTools.waitFor(1000);
        ScreenshotTools.snap("Trending" + postFix);

        onView(withId(R.id.fragment_feed_feed)).perform(RecyclerViewActions.scrollToPosition(2));
        setDate();
        setDayText();
        ViewTools.waitFor(1000);
        ScreenshotTools.snap("OnThisDay" + postFix);

        onView(withId(R.id.fragment_feed_feed)).perform(RecyclerViewActions.scrollToPosition(1));
        setDate();
        setDayText();
        ViewTools.waitFor(1000);
        ScreenshotTools.snap("News" + postFix);
    }

    private static void waitUntilFeedDisplayed() {
        ViewTools.whileWithMaxSteps(
                () -> !ViewTools.viewIsDisplayed(R.id.fragment_feed_feed),
                () -> ViewTools.waitFor(ViewTools.WAIT_FOR_2000));
    }

    private static void setDayText() {
        if (ViewTools.viewIsDisplayed(R.id.day)) {
            onView(withId(R.id.day)).perform(ViewTools.setTextInTextView("February 5"));
        }
    }

    private static void setStaticCardDate() {
        try {
            ViewInteraction colorButton = onView(
                    allOf(
                            ViewTools.matchPosition(allOf(withId(R.id.view_static_card_subtitle)), 0),
                            isDisplayed()));
            colorButton.perform(ViewTools.setTextInTextView("Main page on Feb 5, 2017"));
        } catch (NoMatchingViewException | PerformException e) {
            return;
        }
    }

    private static void setDate() {
        try {
            for (int i = 0; i < 3; i++) {
                ViewInteraction colorButton = onView(
                        allOf(
                                ViewTools.matchPosition(allOf(withId(R.id.view_card_header_subtitle)), i),
                                isDisplayed()));
                colorButton.perform(ViewTools.setTextInTextView("Feb 5, 2017"));
            }
        } catch (NoMatchingViewException | PerformException e) {
            return;
        }
    }
}
