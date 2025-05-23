package com.example.companyloginapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.release;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;

import android.view.View;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ScheduleActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    // Utility matcher to get a view at a specific index
    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: " + index + " ");
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }

    private void performLogin() throws InterruptedException {
        onView(withId(R.id.email)).perform(replaceText("andychen@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(replaceText("123"), closeSoftKeyboard());
        onView(withId(R.id.company_name)).perform(replaceText("WorkSync"), closeSoftKeyboard());
        onView(withId(R.id.company_id)).perform(replaceText("2"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());
        Thread.sleep(2000);
    }

    @Test
    public void testNavigateToSchedulePage() throws InterruptedException {
        init();
        performLogin();
        onView(withId(R.id.button_schedule)).perform(click());
        intended(hasComponent(ScheduleActivity.class.getName()));
        release();
    }

    @Test
    public void testOpenAndCloseScheduleDialog() throws InterruptedException {
        performLogin();
        onView(withId(R.id.button_schedule)).perform(click());
        Thread.sleep(2000);

        // Open the edit schedule dialog
        onView(withId(R.id.edit_time_button)).perform(click());
        Thread.sleep(1000);

        // Click Save without editing any fields
        onView(withText("Save"))
                .inRoot(isDialog())
                .perform(scrollTo(), click());
        Thread.sleep(2000);
    }
}
