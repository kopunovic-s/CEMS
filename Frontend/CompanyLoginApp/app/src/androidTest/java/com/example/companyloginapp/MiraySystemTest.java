package com.example.companyloginapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.allOf;

@RunWith(AndroidJUnit4.class)
public class MiraySystemTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testLoginWithValidCredentials() throws InterruptedException {
        // Start intent capture
        Intents.init();

        // Fill in the login form
        onView(withId(R.id.email)).perform(replaceText("jwill@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(replaceText("123"), closeSoftKeyboard());
        onView(withId(R.id.company_name)).perform(replaceText("ISU"), closeSoftKeyboard());
        onView(withId(R.id.company_id)).perform(replaceText("5"), closeSoftKeyboard());

        // Click the login button
        onView(withId(R.id.login_button)).perform(click());

        // Wait for the network and response
        Thread.sleep(3000);

        // Check that the intent was sent to the DashBoardActivity
        intended(allOf(
                hasComponent(DashBoardActivity.class.getName()),
                hasExtra("companyName", "ISU"),
                hasExtra("companyId", 5)
        ));

        // Finish intent capture
        Intents.release();
    }

    @Test
    public void testButtonsOnDashBoard() throws InterruptedException {
        // Start intent capture
        Intents.init();

        // Fill in the login form
        onView(withId(R.id.email)).perform(replaceText("jwill@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(replaceText("123"), closeSoftKeyboard());
        onView(withId(R.id.company_name)).perform(replaceText("ISU"), closeSoftKeyboard());
        onView(withId(R.id.company_id)).perform(replaceText("5"), closeSoftKeyboard());

        // Click the login button
        onView(withId(R.id.login_button)).perform(click());

        // Wait for login to process and DashBoardActivity to load
        Thread.sleep(3000);

        // Click the Projects button
        onView(withId(R.id.button_projects)).perform(click());

        // Assert that ProjectsActivity is launched
        intended(hasComponent(ProjectsActivity.class.getName()));

        // Finish intent capture
        Intents.release();
    }

    @Test
    public void testNavbar() throws InterruptedException {
        // Start intent capture
        Intents.init();

        // Fill in the login form
        onView(withId(R.id.email)).perform(replaceText("jwill@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(replaceText("123"), closeSoftKeyboard());
        onView(withId(R.id.company_name)).perform(replaceText("ISU"), closeSoftKeyboard());
        onView(withId(R.id.company_id)).perform(replaceText("5"), closeSoftKeyboard());

        // Click the login button
        onView(withId(R.id.login_button)).perform(click());

        // Wait for login to process and DashBoardActivity to load
        Thread.sleep(3000);

        // Open the nav drawer
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());

        // Click on the Projects item
        onView(withId(R.id.nav_projects)).perform(click());

        // Assert intent to ProjectsActivity
        intended(hasComponent(ProjectsActivity.class.getName()));

        Intents.release();
    }

    @Test
    public void testIntent() throws InterruptedException {
        // Start intent capture
        Intents.init();

        // Fill in the login form
        onView(withId(R.id.email)).perform(replaceText("achen23@iastate.edu"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(replaceText("password"), closeSoftKeyboard());
        onView(withId(R.id.company_name)).perform(replaceText("ISU"), closeSoftKeyboard());
        onView(withId(R.id.company_id)).perform(replaceText("1"), closeSoftKeyboard());

        // Click the login button
        onView(withId(R.id.login_button)).perform(click());

        // Wait for login to process and DashBoardActivity to load
        Thread.sleep(3000);

        // Open the nav drawer
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());

        // Click on the Projects item
        onView(withId(R.id.nav_projects)).perform(click());

        // Assert intent to ProjectsActivity
        intended(hasComponent(ProjectsActivity.class.getName()));

        Intents.release();
    }

}

