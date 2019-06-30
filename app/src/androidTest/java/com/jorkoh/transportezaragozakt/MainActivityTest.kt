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

    @Before
    fun init() {
        favoritesRepository.deleteAllFavoriteStops()
    }

    @Test
    fun addFavorite() {
        // Start up the main activity
        ActivityScenario.launch(MainActivity::class.java)

        // Navigate to search destination
        onView(withId(R.id.search_destination)).perform(ViewActions.click())

        // Change tab to all stops
        onView(allOf(withText(R.string.search_all_stops), isDescendantOfA(withId(R.id.search_tab_layout)))).perform(ViewActions.click())
        // Open the first one
        onView(withId(R.id.search_recycler_view_all_stops)).perform(RecyclerViewActions.actionOnItemAtPosition<StopAdapter.StopViewHolder>(0, ViewActions.click()))
        // Favorite it
        onView(withId(R.id.stop_details_fab)).perform(ViewActions.click())
        onView(withId(R.id.stop_details_fab_favorite)).perform(ViewActions.click())
        // Navigate to favorites destination
        pressBack()
        onView(withId(R.id.favorites_destination)).perform(ViewActions.click())

        // Verify task is displayed on screen
        onView(withId(R.id.favorites_recycler_view)).check(ViewAssertions.matches(hasChildCount(1)))
    }

}