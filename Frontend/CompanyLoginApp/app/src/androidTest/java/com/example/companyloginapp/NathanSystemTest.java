package com.example.companyloginapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import static org.hamcrest.Matchers.anything;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.intent.Intents;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NathanSystemTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private void performLogin() throws InterruptedException {
        onView(withId(R.id.email)).perform(replaceText("natewilli@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(replaceText("123"), closeSoftKeyboard());
        onView(withId(R.id.company_name)).perform(replaceText("WorkSync"), closeSoftKeyboard());
        onView(withId(R.id.company_id)).perform(replaceText("2"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());
        Thread.sleep(3000);
    }

    @Test
    public void testOpenProjectDetailsAndCheckChat() throws InterruptedException {
        Intents.init();
        performLogin();

        onView(withId(R.id.button_projects)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.project_input)).perform(replaceText("Test Project"), closeSoftKeyboard());
        onView(withId(R.id.project_due_date_input)).perform(replaceText("2025-12-31"), closeSoftKeyboard());
        onView(withId(R.id.add_project_button)).perform(click());

        Thread.sleep(2000);

        onView(withId(R.id.projects_list)).check(matches(hasMinimumChildCount(1)));
        onData(anything())
                .inAdapterView(withId(R.id.projects_list))
                .atPosition(0)
                .perform(click());

        onView(withId(R.id.chat_input)).check(matches(withText("")));
        Intents.release();
    }

    @Test
    public void testAddProductAndViewAnalytics() throws InterruptedException {
        Intents.init();
        performLogin();

        String uniqueSuffix = String.valueOf(System.currentTimeMillis() % 100000);
        String uniqueDeptName = "Bakery " + uniqueSuffix;
        String uniqueProductName = "Donut " + uniqueSuffix;

        onView(withId(R.id.button_store)).perform(click());

        // Create department first
        onView(withId(R.id.add_department_button)).perform(click());
        onView(withHint("Department Name")).perform(replaceText(uniqueDeptName), closeSoftKeyboard());
        onView(withText("Add")).perform(click());

        Thread.sleep(2000);

        // Select department just created
        onData(anything())
                .inAdapterView(withId(R.id.department_list))
                .atPosition(0)
                .perform(click());

        Thread.sleep(2000);

        // Add product inside that department
        onView(withId(R.id.add_product_button)).perform(click());
        onView(withId(R.id.input_name)).perform(replaceText(uniqueProductName), closeSoftKeyboard());
        onView(withId(R.id.input_cost)).perform(replaceText("0.50"), closeSoftKeyboard());
        onView(withId(R.id.input_price)).perform(replaceText("1.25"), closeSoftKeyboard());
        onView(withId(R.id.input_quantity)).perform(replaceText("4"), closeSoftKeyboard());
        onView(withText("Add")).perform(click());

        Thread.sleep(2000);

        // Verify product exists and go to analytics
        onView(withText(uniqueProductName)).check(matches(isDisplayed()));
        onView(withId(R.id.analytics_button)).perform(click());
        onView(withText("Product Analytics")).check(matches(isDisplayed()));

        Intents.release();
    }

    private static Matcher<View> hasItemWithText(String expectedText) {
        return new BoundedMatcher<View, ViewGroup>(ViewGroup.class) {
            @Override
            protected boolean matchesSafely(ViewGroup viewGroup) {
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    View child = viewGroup.getChildAt(i);
                    if (child instanceof ViewGroup) {
                        if (matchesSafely((ViewGroup) child)) return true;
                    } else if (child instanceof TextView) {
                        if (expectedText.equals(((TextView) child).getText().toString())) return true;
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has item with text: " + expectedText);
            }
        };
    }
}
