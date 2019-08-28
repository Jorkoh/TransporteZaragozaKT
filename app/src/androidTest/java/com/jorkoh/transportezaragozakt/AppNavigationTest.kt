package com.jorkoh.transportezaragozakt

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Test



class AppNavigationTest {

    @Test
    fun navigationBetweenRootDestinations(){
        // Start up the main activity
        ActivityScenario.launch(MainActivity::class.java)

        // Navigate to favorites destination
        onView(withId(R.id.favorites_destination)).perform(ViewActions.click())
        // Check that it's selected on the bottom navigation bar
        onView(withId(R.id.favorites_destination)).check(ViewAssertions.matches(isSelected()))
        // Check that it opened
        onView(withId(R.id.favorites_fragment_container)).check(ViewAssertions.matches(isDisplayed()))
        // Check that the toolbar title is correct
        onView(withId(R.id.fragment_toolbar)).check(ViewAssertions.matches(hasDescendant(withText(R.string.favorites_destination_title))))

        // Navigate to map destination
        onView(withId(R.id.map_destination)).perform(ViewActions.click())
        // Check that it's selected on the bottom navigation bar
        onView(withId(R.id.map_destination)).check(ViewAssertions.matches(isSelected()))
        // Check that it opened
        onView(withId(R.id.map_fragment_container)).check(ViewAssertions.matches(isDisplayed()))
        // Check that the toolbar title is correct
        onView(withId(R.id.fragment_toolbar)).check(ViewAssertions.matches(hasDescendant(withText(R.string.map_destination_title))))

        // Navigate to search destination
        onView(withId(R.id.search_destination)).perform(ViewActions.click())
        // Check that it's selected on the bottom navigation bar
        onView(withId(R.id.search_destination)).check(ViewAssertions.matches(isSelected()))
        // Check that it opened
        onView(withId(R.id.search_fragment_container)).check(ViewAssertions.matches(isDisplayed()))
        // Check that the toolbar title is correct
        onView(withId(R.id.fragment_toolbar)).check(ViewAssertions.matches(hasDescendant(withText(R.string.search_destination_title))))

        // Navigate to reminders destination
        onView(withId(R.id.reminders_destination)).perform(ViewActions.click())
        // Check that it's selected on the bottom navigation bar
        onView(withId(R.id.reminders_destination)).check(ViewAssertions.matches(isSelected()))
        // Check that it opened
        onView(withId(R.id.reminders_fragment_container)).check(ViewAssertions.matches(isDisplayed()))
        // Check that the toolbar title is correct
        onView(withId(R.id.fragment_toolbar)).check(ViewAssertions.matches(hasDescendant(withText(R.string.reminders_destination_title))))

        // Navigate to more destination
        onView(withId(R.id.more_destination)).perform(ViewActions.click())
        // Check that it's selected on the bottom navigation bar
        onView(withId(R.id.more_destination)).check(ViewAssertions.matches(isSelected()))
        // Check that it opened, PreferenceMatchers doesn't seem to work with AndroidX preferences...
        onView(withId(R.id.recycler_view)).check(ViewAssertions.matches(hasDescendant(withText(R.string.style_category_title))))
        // Check that the toolbar title is correct
        onView(withId(R.id.fragment_toolbar)).check(ViewAssertions.matches(hasDescendant(withText(R.string.more_destination_title))))
    }
}