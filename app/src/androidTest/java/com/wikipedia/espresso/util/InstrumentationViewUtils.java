package com.wikipedia.espresso.util;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;

import org.hamcrest.core.AllOf;
import com.wikipedia.R;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static com.wikipedia.espresso.util.ViewTools.WAIT_FOR_1000;
import static com.wikipedia.espresso.util.ViewTools.WAIT_FOR_2000;
import static com.wikipedia.espresso.util.ViewTools.WAIT_FOR_500;
import static com.wikipedia.espresso.util.ViewTools.childAtPosition;
import static com.wikipedia.espresso.util.ViewTools.viewIsDisplayed;
import static com.wikipedia.espresso.util.ViewTools.viewWithTextIsDisplayed;
import static com.wikipedia.espresso.util.ViewTools.waitFor;
import static com.wikipedia.espresso.util.ViewTools.whileWithMaxSteps;

@SuppressWarnings("checkstyle:magicnumber")
public final class InstrumentationViewUtils {
    // TODO: re design the steps of tests

    //Make sure to call the switch from Explore tab
    public static void switchToDarkMode() {
        ViewTools.whileWithMaxSteps(
                () -> !ViewTools.viewIsDisplayed(R.id.fragment_feed_feed),
                () -> ViewTools.waitFor(2000));

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

        ViewInteraction appCompatTextView3 = onView(
                AllOf.allOf(withId(R.id.button_theme_dark), withText("Dark"),
                        ViewTools.childAtPosition(
                                ViewTools.childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                0),
                        isDisplayed()));
        appCompatTextView3.perform(click());

        ViewTools.waitFor(ViewTools.WAIT_FOR_500);
        ViewTools.whileWithMaxSteps(
                () -> !ViewTools.viewIsDisplayed(R.id.fragment_feed_feed),
                Espresso::pressBack);
        ViewTools.waitFor(ViewTools.WAIT_FOR_1000);
    }

    public static void switchToBlackMode() {
        ViewTools.whileWithMaxSteps(
                () -> !ViewTools.viewIsDisplayed(R.id.fragment_feed_feed),
                () -> ViewTools.waitFor(2000));

        // TODO: redesign the way of entering SettingsActivity

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

        ViewInteraction appCompatTextView4 = onView(
                AllOf.allOf(withId(R.id.button_theme_black), withText("Black"),
                        ViewTools.childAtPosition(
                                ViewTools.childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        2),
                                0),
                        isDisplayed()));
        appCompatTextView4.perform(click());

        ViewTools.waitFor(ViewTools.WAIT_FOR_500);
        ViewTools.whileWithMaxSteps(
                () -> !ViewTools.viewIsDisplayed(R.id.fragment_feed_feed),
                Espresso::pressBack);
        ViewTools.waitFor(ViewTools.WAIT_FOR_1000);
    }

    public static void switchPageModeToDark() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction appCompatTextView2 = onView(
                AllOf.allOf(withId(R.id.title), withText("Font and theme"),
                        ViewTools.childAtPosition(
                                ViewTools.childAtPosition(
                                        withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView2.perform(click());

        ViewInteraction appCompatTextView3 = onView(
                AllOf.allOf(withId(R.id.button_theme_dark), withText("Dark"),
                        ViewTools.childAtPosition(
                                ViewTools.childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                0),
                        isDisplayed()));
        appCompatTextView3.perform(click());

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
    }

    public static void switchPageModeToBlack() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction appCompatTextView2 = onView(
                AllOf.allOf(withId(R.id.title), withText("Font and theme"),
                        ViewTools.childAtPosition(
                                ViewTools.childAtPosition(
                                        withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView2.perform(click());

        ViewInteraction appCompatTextView5 = onView(
                AllOf.allOf(withId(R.id.button_theme_black), withText("Black"),
                        ViewTools.childAtPosition(
                                ViewTools.childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        2),
                                0),
                        isDisplayed()));
        appCompatTextView5.perform(click());

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
    }

    private InstrumentationViewUtils() {
    }
}
