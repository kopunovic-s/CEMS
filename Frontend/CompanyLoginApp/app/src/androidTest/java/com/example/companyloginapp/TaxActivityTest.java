package com.example.companyloginapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TaxActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private void loginAsAndyChen() throws InterruptedException {
        onView(withId(R.id.email)).perform(replaceText("andychen@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(replaceText("123"), closeSoftKeyboard());
        onView(withId(R.id.company_name)).perform(replaceText("WorkSync"), closeSoftKeyboard());
        onView(withId(R.id.company_id)).perform(replaceText("2"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());
        Thread.sleep(2000); // Wait for dashboard to load
    }

    private void openTaxPage() throws InterruptedException {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_tax));
        Thread.sleep(2000); // Wait for TaxActivity to load
    }

    @Test
    public void testFetchAndDownloadW2_2025() throws InterruptedException {
        loginAsAndyChen();
        openTaxPage();

        // Enter tax year and fetch
        onView(withId(R.id.input_tax_year)).perform(replaceText("2025"), closeSoftKeyboard());
        onView(withId(R.id.button_fetch_tax)).perform(click());
        Thread.sleep(3000); // Wait for data to load

        // Verify data appeared
        onView(withId(R.id.tax_info_text))
                .check(matches(withText(Matchers.containsString("Year: 2025"))));

        // Attempt to download PDF
        onView(withId(R.id.button_download_pdf)).perform(click());
        Thread.sleep(3000); // Give time for download
    }
}
