package com.wikipedia.espresso.main.overflow;

import android.Manifest;
import android.view.View;

import com.wikipedia.espresso.util.CompareTools;
import com.wikipedia.espresso.util.InstrumentationViewUtils;
import com.wikipedia.espresso.util.ScreenshotTools;
import com.wikipedia.espresso.util.ViewTools;
import com.wikipedia.main.MainActivity;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.core.AllOf;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.wikipedia.R;
import com.wikipedia.espresso.util.ScreenshotTools;
import com.wikipedia.espresso.util.ViewTools;
import com.wikipedia.main.MainActivity;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static com.wikipedia.espresso.util.CompareTools.assertScreenshotWithinTolerance;
import static com.wikipedia.espresso.util.InstrumentationViewUtils.switchToBlackMode;
import static com.wikipedia.espresso.util.InstrumentationViewUtils.switchToDarkMode;
import static com.wikipedia.espresso.util.ViewTools.WAIT_FOR_1000;
import static com.wikipedia.espresso.util.ViewTools.WAIT_FOR_2000;
import static com.wikipedia.espresso.util.ViewTools.WAIT_FOR_500;
import static com.wikipedia.espresso.util.ViewTools.childAtPosition;
import static com.wikipedia.espresso.util.ViewTools.viewIsDisplayed;
import static com.wikipedia.espresso.util.ViewTools.waitFor;
import static com.wikipedia.espresso.util.ViewTools.whileWithMaxSteps;

@RunWith(AndroidJUnit4.class)
@SuppressWarnings("checkstyle:magicnumber")
public class CustomizeFeedTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);
    @Rule
    public GrantPermissionRule runtimePermissionRule = GrantPermissionRule.grant(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void customizeFeed() throws Exception {
        runTests("");
        InstrumentationViewUtils.switchToDarkMode();
        runTests("_Dark");
        InstrumentationViewUtils.switchToBlackMode();
        runTests("_Black");
        runComparisons();
    }

    private void runTests(String mode) {
        ViewTools.whileWithMaxSteps(
                () -> !ViewTools.viewIsDisplayed(R.id.fragment_feed_feed),
                () -> ViewTools.waitFor(ViewTools.WAIT_FOR_2000));

        // TODO: redesign the way of entering Configure Feed page

        ViewTools.waitFor(ViewTools.WAIT_FOR_500);
        ScreenshotTools.snap("CustomizeFeed1Of2" + mode);
        ViewTools.waitFor(ViewTools.WAIT_FOR_500);
        onView(withId(R.id.content_types_recycler)).perform(RecyclerViewActions.scrollToPosition(8));
        ViewTools.waitFor(ViewTools.WAIT_FOR_500);
        ScreenshotTools.snap("CustomizeFeed2Of2" + mode);
        ViewTools.waitFor(ViewTools.WAIT_FOR_500);
        onView(withId(R.id.content_types_recycler)).perform(RecyclerViewActions.scrollToPosition(0));
        ViewTools.waitFor(ViewTools.WAIT_FOR_1000);
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        ViewTools.waitFor(ViewTools.WAIT_FOR_2000);
        ScreenshotTools.snap("CustomizeOverflowMenu" + mode);
        ViewInteraction appCompatTextView3 = onView(
                AllOf.allOf(withId(R.id.title), withText("Deselect all"),
                        ViewTools.childAtPosition(
                                ViewTools.childAtPosition(
                                        withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView3.perform(click());
        ViewTools.waitFor(ViewTools.WAIT_FOR_2000);
        ScreenshotTools.snap("CustomizeDeselectAll" + mode);
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        ViewInteraction appCompatTextView4 = onView(
                AllOf.allOf(withId(R.id.title), withText("Select all"),
                        ViewTools.childAtPosition(
                                ViewTools.childAtPosition(
                                        withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView4.perform(click());
        ViewTools.waitFor(ViewTools.WAIT_FOR_2000);
        ScreenshotTools.snap("CustomizeSelectAll" + mode);
        ViewTools.waitFor(ViewTools.WAIT_FOR_2000);
        onView(withId(R.id.content_types_recycler)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, ViewTools.clickChildViewWithId(R.id.feed_content_type_checkbox)));
        ViewInteraction appCompatImageButton = onView(
                AllOf.allOf(withContentDescription("Navigate up"),
                        ViewTools.childAtPosition(
                                AllOf.allOf(withId(R.id.action_bar),
                                        ViewTools.childAtPosition(
                                                withId(R.id.action_bar_container),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton.perform(click());
        ViewInteraction textView = onView(
                AllOf.allOf(withId(R.id.view_card_header_title), withText("On this day"),
                        ViewTools.childAtPosition(
                                AllOf.allOf(withId(R.id.view_on_this_day_card_header),
                                        ViewTools.childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
                                                0)),
                                1),
                        isDisplayed()));
        textView.check(matches(withText("On this day")));
        ViewTools.waitFor(ViewTools.WAIT_FOR_2000);

        // TODO: redesign the way of entering Configure Feed page

        ViewTools.waitFor(ViewTools.WAIT_FOR_2000);
        onView(withId(R.id.content_types_recycler)).perform(
                RecyclerViewActions.actionOnItemAtPosition(0, ViewTools.clickChildViewWithId(R.id.feed_content_type_checkbox)));
        ViewInteraction appCompatImageButton2 = onView(
                AllOf.allOf(withContentDescription("Navigate up"),
                        ViewTools.childAtPosition(
                                AllOf.allOf(withId(R.id.action_bar),
                                        ViewTools.childAtPosition(
                                                withId(R.id.action_bar_container),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatImageButton2.perform(click());
        ViewInteraction textView2 = onView(
                AllOf.allOf(withId(R.id.view_card_header_title), withText("In the news"),
                        ViewTools.childAtPosition(
                                AllOf.allOf(withId(R.id.view_list_card_header),
                                        ViewTools.childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                                0)),
                                1),
                        isDisplayed()));
        textView2.check(matches(withText("In the news")));
    }

    private void runComparisons() throws Exception {
        CompareTools.assertScreenshotWithinTolerance("CustomizeFeed1Of2");
        CompareTools.assertScreenshotWithinTolerance("CustomizeFeed1Of2_Dark");
        CompareTools.assertScreenshotWithinTolerance("CustomizeFeed1Of2_Black");
        CompareTools.assertScreenshotWithinTolerance("CustomizeFeed2Of2");
        CompareTools.assertScreenshotWithinTolerance("CustomizeFeed2Of2_Dark");
        CompareTools.assertScreenshotWithinTolerance("CustomizeFeed2Of2_Black");
        CompareTools.assertScreenshotWithinTolerance("CustomizeSelectAll");
        CompareTools.assertScreenshotWithinTolerance("CustomizeSelectAll_Dark");
        CompareTools.assertScreenshotWithinTolerance("CustomizeSelectAll_Black");
        CompareTools.assertScreenshotWithinTolerance("CustomizeDeselectAll");
        CompareTools.assertScreenshotWithinTolerance("CustomizeDeselectAll_Dark");
        CompareTools.assertScreenshotWithinTolerance("CustomizeDeselectAll_Black");
        CompareTools.assertScreenshotWithinTolerance("CustomizeOverflowMenu");
        CompareTools.assertScreenshotWithinTolerance("CustomizeOverflowMenu_Dark");
        CompareTools.assertScreenshotWithinTolerance("CustomizeOverflowMenu_Black");
    }
}
