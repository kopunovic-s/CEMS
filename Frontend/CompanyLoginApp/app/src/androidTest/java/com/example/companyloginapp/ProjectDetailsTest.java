package com.example.companyloginapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import static org.hamcrest.Matchers.anything;
import static org.junit.Assert.assertTrue;

import android.widget.DatePicker;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.contrib.PickerActions;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

@RunWith(AndroidJUnit4.class)
public class ProjectDetailsTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private void loginAndEnterProjectDetails() throws InterruptedException {
        onView(withId(R.id.email)).perform(replaceText("natewilli@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(replaceText("123"), closeSoftKeyboard());
        onView(withId(R.id.company_name)).perform(replaceText("WorkSync"), closeSoftKeyboard());
        onView(withId(R.id.company_id)).perform(replaceText("2"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());

        Thread.sleep(3000);
        onView(withId(R.id.button_projects)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.project_input)).perform(replaceText("Test Project"), closeSoftKeyboard());
        onView(withId(R.id.project_due_date_input)).perform(replaceText("2025-12-31"), closeSoftKeyboard());
        onView(withId(R.id.add_project_button)).perform(click());

        Thread.sleep(2000);
        onData(anything()).inAdapterView(withId(R.id.projects_list)).atPosition(0).perform(click());
        Thread.sleep(2000);
    }

    @Test
    public void testProjectFieldsEditableAndDisplayed() throws InterruptedException {
        loginAndEnterProjectDetails();
        onView(withId(R.id.project_name)).check(matches(isDisplayed()));
        onView(withId(R.id.project_description)).perform(replaceText("Updated description"), closeSoftKeyboard());
    }

    @Test
    public void testDueDatePickerOpens() throws InterruptedException {
        loginAndEnterProjectDetails();
        onView(withId(R.id.project_due_date)).perform(click());
        onView(Matchers.instanceOf(DatePicker.class)).perform(PickerActions.setDate(2025, 12, 25));
    }

    @Test
    public void testAssignedUserDropdownOpens() throws InterruptedException {
        loginAndEnterProjectDetails();
        onView(withId(R.id.assigned_users)).perform(click());
    }



    @Test
    public void testSaveChanges() throws InterruptedException {
        loginAndEnterProjectDetails();
        onView(withId(R.id.project_description)).perform(replaceText("Updated from test"), closeSoftKeyboard());
        onView(withId(R.id.save_button)).perform(click());
        Thread.sleep(1500);
    }

    @Test
    public void testToggleProjectStatus() throws InterruptedException {
        loginAndEnterProjectDetails();
        onView(withId(R.id.toggle_status_button)).perform(click());
        Thread.sleep(1500);
    }

    @Test
    public void testMarkProjectAsComplete() throws InterruptedException {
        loginAndEnterProjectDetails();
        onView(withId(R.id.complete_project_button)).perform(click());
        Thread.sleep(1500);
    }


}
