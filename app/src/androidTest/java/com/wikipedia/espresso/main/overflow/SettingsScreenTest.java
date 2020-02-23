package com.wikipedia.espresso.main.overflow;


import android.Manifest;

import com.wikipedia.espresso.util.CompareTools;
import com.wikipedia.espresso.util.ScreenshotTools;
import com.wikipedia.espresso.util.ViewTools;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.core.AllOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.wikipedia.R;
import com.wikipedia.espresso.util.ScreenshotTools;
import com.wikipedia.settings.SettingsActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static com.wikipedia.espresso.util.CompareTools.assertScreenshotWithinTolerance;
import static com.wikipedia.espresso.util.ViewTools.WAIT_FOR_1000;
import static com.wikipedia.espresso.util.ViewTools.WAIT_FOR_2000;
import static com.wikipedia.espresso.util.ViewTools.WAIT_FOR_500;
import static com.wikipedia.espresso.util.ViewTools.childAtPosition;
import static com.wikipedia.espresso.util.ViewTools.viewWithTextIsDisplayed;
import static com.wikipedia.espresso.util.ViewTools.waitFor;
import static com.wikipedia.espresso.util.ViewTools.whileWithMaxSteps;

@RunWith(AndroidJUnit4.class)
@SuppressWarnings("checkstyle:magicnumber")
public class SettingsScreenTest {

    @Rule
    public ActivityTestRule<SettingsActivity> activityTestRule = new ActivityTestRule<>(SettingsActivity.class);

    @Rule
    public GrantPermissionRule runtimePermissionRule = GrantPermissionRule.grant(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void settingTest() throws Exception {

        // Wait for Screen display
        ViewTools.whileWithMaxSteps(
                () -> !ViewTools.viewWithTextIsDisplayed("General"),
                () -> ViewTools.waitFor(ViewTools.WAIT_FOR_2000));

        //Click App Theme
        ViewInteraction recyclerView = onView(
                AllOf.allOf(withId(android.R.id.list),
                        ViewTools.childAtPosition(
                                withId(android.R.id.list_container),
                                0)));
        recyclerView.perform(actionOnItemAtPosition(3, click()));

        ViewTools.waitFor(ViewTools.WAIT_FOR_2000);
        ScreenshotTools.snap("AppThemeChangeScreenLight");

        ViewInteraction appCompatTextView3 = onView(
                AllOf.allOf(withId(R.id.button_theme_dark), withText("Dark"),
                        ViewTools.childAtPosition(
                                ViewTools.childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                0),
                        isDisplayed()));
        appCompatTextView3.perform(click());
        ViewTools.waitFor(ViewTools.WAIT_FOR_2000);
        ScreenshotTools.snap("AppThemeChangeScreenDark");

        ViewInteraction view = onView(
                AllOf.allOf(withId(R.id.touch_outside),
                        ViewTools.childAtPosition(
                                AllOf.allOf(withId(R.id.coordinator),
                                        ViewTools.childAtPosition(
                                                withId(R.id.container),
                                                0)),
                                0),
                        isDisplayed()));
        view.perform(click());
        ViewTools.waitFor(ViewTools.WAIT_FOR_1000);

        takeSettingsPageScreenshots("_Dark");
        recyclerView.perform(actionOnItemAtPosition(3, click()));
        ViewTools.waitFor(ViewTools.WAIT_FOR_2000);

        ViewInteraction appCompatTextView4 = onView(
                AllOf.allOf(withId(R.id.button_theme_black), withText("Black"),
                        ViewTools.childAtPosition(
                                ViewTools.childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        2),
                                0),
                        isDisplayed()));
        appCompatTextView4.perform(click());

        ViewTools.waitFor(ViewTools.WAIT_FOR_2000);
        ScreenshotTools.snap("AppThemeChangeScreenBlack");
        view.perform(click());
        ViewTools.waitFor(ViewTools.WAIT_FOR_1000);

        takeSettingsPageScreenshots("_Black");
        recyclerView.perform(actionOnItemAtPosition(3, click()));

        ViewInteraction appCompatTextView5 = onView(
                AllOf.allOf(withId(R.id.button_theme_light), withText("Light"),
                        ViewTools.childAtPosition(
                                ViewTools.childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView5.perform(click());
        ViewTools.waitFor(ViewTools.WAIT_FOR_500);

        view.perform(click());
        ViewTools.waitFor(ViewTools.WAIT_FOR_1000);
        takeSettingsPageScreenshots("");
        runComparisons();

    }

    private void takeSettingsPageScreenshots(String mode) {
        ScreenshotTools.snap("SettingsScreen1of3" + mode);
        onView(withId(android.R.id.list)).perform(RecyclerViewActions.scrollToPosition(10));
        ViewTools.waitFor(ViewTools.WAIT_FOR_1000);
        ScreenshotTools.snap("SettingsScreen2of3" + mode);
        onView(withId(android.R.id.list)).perform(RecyclerViewActions.scrollToPosition(18));
        ViewTools.waitFor(ViewTools.WAIT_FOR_1000);
        ScreenshotTools.snap("SettingsScreen3of3" + mode);
        onView(withId(android.R.id.list)).perform(RecyclerViewActions.scrollToPosition(0));
    }

    private void runComparisons() throws Exception {
        CompareTools.assertScreenshotWithinTolerance("SettingsScreen1of3");
        CompareTools.assertScreenshotWithinTolerance("SettingsScreen1of3_Dark");
        CompareTools.assertScreenshotWithinTolerance("SettingsScreen1of3_Black");
        CompareTools.assertScreenshotWithinTolerance("SettingsScreen2of3");
        CompareTools.assertScreenshotWithinTolerance("SettingsScreen2of3_Dark");
        CompareTools.assertScreenshotWithinTolerance("SettingsScreen2of3_Black");
        CompareTools.assertScreenshotWithinTolerance("SettingsScreen3of3");
        CompareTools.assertScreenshotWithinTolerance("SettingsScreen3of3_Dark");
        CompareTools.assertScreenshotWithinTolerance("SettingsScreen3of3_Black");
        CompareTools.assertScreenshotWithinTolerance("AppThemeChangeScreenLight");
        CompareTools.assertScreenshotWithinTolerance("AppThemeChangeScreenDark");
        CompareTools.assertScreenshotWithinTolerance("AppThemeChangeScreenBlack");
    }

}
