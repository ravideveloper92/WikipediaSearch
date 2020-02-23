package com.wikipedia.espresso.reading;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.wikipedia.WikipediaApp;
import com.wikipedia.main.MainActivity;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.wikipedia.R;
import com.wikipedia.WikipediaApp;
import com.wikipedia.espresso.page.PageActivityTest;
import com.wikipedia.espresso.search.SearchBehaviors;
import com.wikipedia.espresso.util.ScreenshotTools;
import com.wikipedia.espresso.util.ViewTools;
import com.wikipedia.main.MainActivity;
import com.wikipedia.navtab.NavTab;
import com.wikipedia.readinglist.database.ReadingListDbHelper;
import com.wikipedia.settings.PrefsIoUtil;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.core.AllOf.allOf;
import static com.wikipedia.espresso.util.CompareTools.assertScreenshotWithinTolerance;
import static com.wikipedia.espresso.util.InstrumentationViewUtils.switchToBlackMode;
import static com.wikipedia.espresso.util.InstrumentationViewUtils.switchToDarkMode;
import static com.wikipedia.espresso.util.ViewTools.WAIT_FOR_1000;
import static com.wikipedia.espresso.util.ViewTools.WAIT_FOR_2000;
import static com.wikipedia.espresso.util.ViewTools.WAIT_FOR_500;
import static com.wikipedia.espresso.util.ViewTools.WAIT_FOR_6000;
import static com.wikipedia.espresso.util.ViewTools.childAtPosition;
import static com.wikipedia.espresso.util.ViewTools.pressBack;
import static com.wikipedia.espresso.util.ViewTools.selectTab;
import static com.wikipedia.espresso.util.ViewTools.viewIsDisplayed;
import static com.wikipedia.espresso.util.ViewTools.waitFor;
import static com.wikipedia.espresso.util.ViewTools.whileWithMaxSteps;

@RunWith(AndroidJUnit4.class)
@SuppressWarnings("checkstyle:methodlength")
public final class ReadingListsTest {
    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule runtimePermissionRule = GrantPermissionRule.grant(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void testReadingLists() throws Exception {
        navigateToReadingListsFromExploreTab();
        runTests("");
        clearLists();
        switchToDarkMode();
        navigateToReadingListsFromExploreTab();
        runTests("_Dark");
        clearLists();
        switchToBlackMode();
        navigateToReadingListsFromExploreTab();
        runTests("_Black");
        clearLists();
        runComparisons("");
        runComparisons("_Dark");
        runComparisons("_Black");
    }

    private void clearLists() {
        navigateToExploreTab();

        ReadingListDbHelper.instance().resetToDefaults();
        PrefsIoUtil.setBoolean(R.string.preference_key_toc_tutorial_enabled, false);
        PrefsIoUtil.setBoolean(R.string.preference_key_description_edit_tutorial_enabled, false);
    }

    private void runTests(String mode) {
        if (viewIsDisplayed(R.id.view_onboarding_action_negative)) {
            onView(ViewTools.first(withId(R.id.view_onboarding_action_negative))).perform(click());
        }

        captureInitialReadingTabViews(mode);
        captureAddingArticleToDefaultList(mode);

        navigateToReadingListsFromExploreTab();

        captureDefaultListViews(mode);
        pressBack();

        captureMyListsTabWithUserCreatedLists(mode);
    }


    private void captureMyListsTabWithUserCreatedLists(String mode) {
        onView(withId(R.id.menu_search_lists)).perform(click());

        onView(withId(R.id.search_src_text)).perform(typeText("obama"));
        waitFor(WAIT_FOR_1000);

        ScreenshotTools.snap("ReadingListSearchWithResults" + mode);
        waitFor(WAIT_FOR_1000);

        onView(withId(R.id.search_src_text)).perform(typeText("oooo"));
        ScreenshotTools.snap("ReadingListSearchWithNoResults" + mode);

        pressBack();
        pressBack();
        waitFor(WAIT_FOR_1000);
        onView(withId(R.id.menu_sort_options)).perform(click());
        ScreenshotTools.snap("ReadingListsSortMenu" + mode);
        waitFor(WAIT_FOR_1000);

        pressBack();
        waitFor(WAIT_FOR_1000);
        onView(withId(R.id.menu_search_lists)).perform(click());
        onView(withId(R.id.search_src_text)).perform(typeText("my"));
        waitFor(WAIT_FOR_1000);
        ScreenshotTools.snap("ReadingListsSearchWithResults" + mode);
        waitFor(WAIT_FOR_1000);

        onView(withId(R.id.search_src_text)).perform(typeText("ooo"));
        ScreenshotTools.snap("ReadingListsSearchWithNoResults" + mode);
        waitFor(WAIT_FOR_1000);

        pressBack();
        waitFor(WAIT_FOR_1000);
        ScreenshotTools.snap("ReadingListsFragmentWithNoEmptyStateMessages" + mode);
    }

    private void navigateToExploreTab() {
        whileWithMaxSteps(
                () -> !viewIsDisplayed(R.id.fragment_main_nav_tab_layout),
                () -> waitFor(WAIT_FOR_2000));

        onView(withId(R.id.fragment_main_nav_tab_layout))
                .perform(selectTab(NavTab.EXPLORE.code()))
                .check(matches(isDisplayed()));
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(WikipediaApp.getInstance());
        prefs.edit().clear().commit();
        waitFor(WAIT_FOR_2000);
    }

    private void captureDefaultListViews(String mode) {
        ScreenshotTools.snap("ReadingListsFragmentNonEmptyDefaultList" + mode);
        waitFor(WAIT_FOR_1000);
        onView(withId(R.id.reading_list_list)).perform(RecyclerViewActions.scrollToPosition(0)).perform(click());
        waitFor(WAIT_FOR_1000);
        ScreenshotTools.snap("DefaultListNonEmpty" + mode);
        waitFor(WAIT_FOR_1000);
        onView(withText("Remove all from offline")).perform(click());
        waitFor(WAIT_FOR_500);
        ScreenshotTools.snap("ReadingListRemoveOffline" + mode);
        waitFor(WAIT_FOR_1000);
    }

    private void captureAddingArticleToDefaultList(String mode) {
        PageActivityTest pageActivityTest = new PageActivityTest();
        Intent intent = new Intent();
        pageActivityTest.activityTestRule.launchActivity(intent);
        waitFor(WAIT_FOR_6000);

        SearchBehaviors.searchKeywordAndGo("Barack Obama", false);
        whileWithMaxSteps(
                () -> !viewIsDisplayed(R.id.search_results_list),
                () -> waitFor(WAIT_FOR_1000),
                10);


        DataInteraction view = onData(anything())
                .inAdapterView(allOf(withId(R.id.search_results_list),
                        childAtPosition(
                                withId(R.id.search_results_container),
                                1)))
                .atPosition(0);
        view.perform(click());
        waitFor(WAIT_FOR_1000);


        onView(withId(R.id.page_actions_tab_layout))
                .perform(selectTab(0));
        waitFor(WAIT_FOR_1000);

        ScreenshotTools.snap("AddToReadingListFirstTimeRationale" + mode);
        waitFor(WAIT_FOR_500);

        if (viewIsDisplayed(R.id.onboarding_button)) {
            onView(ViewTools.first(withText("Got it"))).perform(click());
        }
        whileWithMaxSteps(
                () -> !viewIsDisplayed(R.id.list_of_lists),
                () -> waitFor(WAIT_FOR_1000),
                10);

        onView(withId(R.id.list_of_lists)).perform(RecyclerViewActions.scrollToPosition(0)).perform(click());
        waitFor(WAIT_FOR_1000);
        ScreenshotTools.snap("ObamaSavedToDefaultListRationale" + mode);
        waitFor(WAIT_FOR_1000);
        pressBack();
    }

    private void captureInitialReadingTabViews(String mode) {
        waitFor(WAIT_FOR_1000);
        ScreenshotTools.snap("ReadingTab" + mode);
        waitFor(WAIT_FOR_2000);

        onView(withId(R.id.reading_list_list)).perform(RecyclerViewActions.scrollToPosition(0)).perform(click());
        waitFor(WAIT_FOR_2000);
        ScreenshotTools.snap("DefaultListEmpty" + mode);
        pressBack();
        whileWithMaxSteps(
                () -> !viewIsDisplayed(R.id.fragment_main_nav_tab_layout),
                () -> waitFor(WAIT_FOR_2000),
                10);
    }

    private void runComparisons(String mode) throws Exception {
        assertScreenshotWithinTolerance("ReadingTab" + mode);
        assertScreenshotWithinTolerance("DefaultListEmpty" + mode);
        assertScreenshotWithinTolerance("AddToReadingListFirstTimeRationale" + mode);
        assertScreenshotWithinTolerance("ObamaSavedToDefaultListRationale" + mode);
        assertScreenshotWithinTolerance("DefaultListNonEmpty" + mode);
        assertScreenshotWithinTolerance("ReadingListsFragmentNonEmptyDefaultList" + mode);
        assertScreenshotWithinTolerance("DefaultListOverflow" + mode);
        assertScreenshotWithinTolerance("ReadingListSaveOffline" + mode);
        assertScreenshotWithinTolerance("ReadingListRemoveOffline" + mode);
        assertScreenshotWithinTolerance("ReadingListFragmentArticleOverflow" + mode);
        assertScreenshotWithinTolerance("AddToAnotherListDialog" + mode);
        assertScreenshotWithinTolerance("CreateNewReadingListDialog" + mode);
        assertScreenshotWithinTolerance("CreateNewReadingListDialogNameError" + mode);
        assertScreenshotWithinTolerance("ArticleDeletedRationale" + mode);
        assertScreenshotWithinTolerance("ListOverflow" + mode);
        assertScreenshotWithinTolerance("ListRenameDialog" + mode);
        assertScreenshotWithinTolerance("ListEditDescDialog" + mode);
        assertScreenshotWithinTolerance("ListWithDesc" + mode);
        assertScreenshotWithinTolerance("ListEditDescDialogWithPreviousText" + mode);
        assertScreenshotWithinTolerance("ListDeleteConfirmationDialog" + mode);
        assertScreenshotWithinTolerance("ReadingListSortMenu" + mode);
        assertScreenshotWithinTolerance("ReadingListSearchWithResults" + mode);
        assertScreenshotWithinTolerance("ReadingListSearchWithNoResults" + mode);
        assertScreenshotWithinTolerance("ReadingListsSortMenu" + mode);
        assertScreenshotWithinTolerance("ReadingListsSearchWithResults" + mode);
        assertScreenshotWithinTolerance("ReadingListsSearchWithNoResults" + mode);
        assertScreenshotWithinTolerance("ReadingListsFragmentWithNoEmptyStateMessages" + mode);
    }

    private static void navigateToReadingListsFromExploreTab() {
        whileWithMaxSteps(
                () -> !viewIsDisplayed(R.id.fragment_main_nav_tab_layout),
                ViewTools::pressBack);
        whileWithMaxSteps(
                () -> !viewIsDisplayed(R.id.fragment_main_nav_tab_layout),
                () -> waitFor(WAIT_FOR_2000));

        onView(withId(R.id.fragment_main_nav_tab_layout))
                .perform(selectTab(NavTab.READING_LISTS.code()))
                .check(matches(isDisplayed()));

        waitFor(WAIT_FOR_2000);
    }

}
