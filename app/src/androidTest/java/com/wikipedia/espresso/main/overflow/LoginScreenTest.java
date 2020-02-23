package com.wikipedia.espresso.main.overflow;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import com.wikipedia.espresso.util.CompareTools;
import com.wikipedia.espresso.util.ScreenshotTools;
import com.wikipedia.espresso.util.ViewTools;

import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.ViewInteraction;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.hamcrest.core.AllOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.wikipedia.R;
import com.wikipedia.espresso.util.ScreenshotTools;
import com.wikipedia.login.LoginActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;
import static com.wikipedia.espresso.util.CompareTools.assertScreenshotWithinTolerance;
import static com.wikipedia.espresso.util.ViewTools.WAIT_FOR_1000;
import static com.wikipedia.espresso.util.ViewTools.WAIT_FOR_2000;
import static com.wikipedia.espresso.util.ViewTools.WAIT_FOR_500;
import static com.wikipedia.espresso.util.ViewTools.childAtPosition;
import static com.wikipedia.espresso.util.ViewTools.rotateScreen;
import static com.wikipedia.espresso.util.ViewTools.waitFor;
import static com.wikipedia.login.LoginActivity.LOGIN_REQUEST_SOURCE;

@RunWith(AndroidJUnit4.class)
@SuppressWarnings("checkstyle:magicnumber")
public class LoginScreenTest {

    @Rule
    public ActivityTestRule<LoginActivity> activityTestRule =
            new ActivityTestRule<LoginActivity>(LoginActivity.class) {
                @Override
                protected Intent getActivityIntent() {
                    Context targetContext = InstrumentationRegistry.getInstrumentation()
                            .getTargetContext();
                    Intent result = new Intent(targetContext, LoginActivity.class);
                    result.putExtra(LOGIN_REQUEST_SOURCE, "Test");
                    return result;
                }
            };

    @Rule
    public GrantPermissionRule runtimePermissionRule = GrantPermissionRule.grant(
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void loginScreenTest() throws Exception {
        ViewTools.waitFor(ViewTools.WAIT_FOR_2000);

        ViewInteraction plainPasteEditText = onView(
                AllOf.allOf(ViewTools.childAtPosition(
                        ViewTools.childAtPosition(
                                withId(R.id.login_username_text),
                                0),
                        0),
                        isDisplayed()));
        plainPasteEditText.perform(replaceText("xxx"), closeSoftKeyboard());

        ViewInteraction plainPasteEditText2 = onView(
                AllOf.allOf(ViewTools.childAtPosition(
                        ViewTools.childAtPosition(
                                withId(R.id.login_password_input),
                                0),
                        0),
                        isDisplayed()));
        plainPasteEditText2.perform(replaceText("xxx"), closeSoftKeyboard());
        ViewTools.waitFor(ViewTools.WAIT_FOR_2000);
        ScreenshotTools.snap("LoginScreen");

        ViewTools.rotateScreen(activityTestRule.getActivity(), ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        ViewTools.waitFor(ViewTools.WAIT_FOR_1000);
        ScreenshotTools.snap("LoginScreen_Landscape");
        ViewTools.rotateScreen(activityTestRule.getActivity(), ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewTools.waitFor(ViewTools.WAIT_FOR_500);
        onView(withText("Join Wikipedia")).perform(click());
        ViewTools.waitFor(ViewTools.WAIT_FOR_2000);
        ScreenshotTools.snap("CreateAccountScreen");
        runComparisons();
    }

    private void runComparisons() throws Exception {
        CompareTools.assertScreenshotWithinTolerance("LoginScreen");
        CompareTools.assertScreenshotWithinTolerance("LoginScreen_Landscape");
        CompareTools.assertScreenshotWithinTolerance("CreateAccountScreen");
    }
}
