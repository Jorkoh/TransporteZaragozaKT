package com.jorkoh.transportezaragozakt

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.jorkoh.transportezaragozakt.destinations.search.StopAdapter
import com.jorkoh.transportezaragozakt.repositories.FavoritesRepository
import com.jorkoh.transportezaragozakt.repositories.RemindersRepository
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest : KoinTest {

    private val favoritesRepository: FavoritesRepository by inject()
    private val remindersRepository: RemindersRepository by inject()

    @Before
    fun init() {
        runBlocking {
            favoritesRepository.deleteAllFavoriteStops()
        }
        remindersRepository.deleteAllReminders()
    }

    @Test
    fun addFavorite() {
        // Start up the main activity
        ActivityScenario.launch(MainActivity::class.java)
        // Navigate to favorites destinations
        onView(withId(R.id.favorites_destination)).perform(ViewActions.click())
        //Verify that there are no favorites displayed on screen
        onView(withId(R.id.favorites_recycler_view)).check(ViewAssertions.matches(hasChildCount(0)))

        // Navigate to search destination
        onView(withId(R.id.search_destination)).perform(ViewActions.click())
        // Change tab to all stops
        onView(allOf(withText(R.string.search_all_stops), isDescendantOfA(withId(R.id.search_tab_layout)))).perform(ViewActions.click())
        // Open the first one
        onView(withId(R.id.search_recycler_view_all_stops)).perform(
            RecyclerViewActions.actionOnItemAtPosition<StopAdapter.StopViewHolder>(
                0,
                ViewActions.click()
            )
        )
        // Favorite it
        onView(withId(R.id.stop_details_fab)).perform(ViewActions.click())
        onView(withId(R.id.stop_details_fab_favorite)).perform(ViewActions.click())
        // Check that snackbar appears
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(ViewAssertions.matches(withText(R.string.added_favorite_snackbar)))

        // Navigate to favorites destination
        pressBack()
        onView(withId(R.id.favorites_destination)).perform(ViewActions.click())
        // Verify that the new favorite is displayed on screen
        onView(withId(R.id.favorites_recycler_view)).check(ViewAssertions.matches(hasChildCount(1)))
    }

    @Test
    fun addReminder() {
        // Start up the main activity
        ActivityScenario.launch(MainActivity::class.java)
        // Navigate to reminders destination
        onView(withId(R.id.reminders_destination)).perform(ViewActions.click())
        // Verify that there are no reminders displayed on screen
        onView(withId(R.id.reminders_recycler_view)).check(ViewAssertions.matches(hasChildCount(0)))

        // Navigate to search destination
        onView(withId(R.id.search_destination)).perform(ViewActions.click())
        // Change tab to all stops
        onView(allOf(withText(R.string.search_all_stops), isDescendantOfA(withId(R.id.search_tab_layout)))).perform(ViewActions.click())
        // Open the first one
        onView(withId(R.id.search_recycler_view_all_stops)).perform(
            RecyclerViewActions.actionOnItemAtPosition<StopAdapter.StopViewHolder>(
                0,
                ViewActions.click()
            )
        )
        // Create a reminder
        onView(withId(R.id.stop_details_fab)).perform(ViewActions.click())
        onView(withId(R.id.stop_details_fab_reminder)).perform(ViewActions.click())
        onView(withText(R.string.create_button)).perform(ViewActions.click())
        // Check that snackbar appears
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(ViewAssertions.matches(withText(R.string.added_reminder_snackbar)))

        // Navigate to reminders destination
        pressBack()
        onView(withId(R.id.reminders_destination)).perform(ViewActions.click())
        // Verify that the new reminder is displayed on screen
        onView(withId(R.id.reminders_recycler_view)).check(ViewAssertions.matches(hasChildCount(1)))
    }

}