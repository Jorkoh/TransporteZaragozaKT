package com.jorkoh.transportezaragozakt.navigation

import android.view.Menu
import androidx.fragment.app.FragmentManager

fun needsCustomBackHandling(
    myBackStack: MutableList<Destinations>,
    supportFragmentManager: FragmentManager
): Boolean {
    return supportFragmentManager.backStackEntryCount == 0 && myBackStack.count() > 1 && !isDoubleHome(
        myBackStack
    )
}

private fun isDoubleHome(myBackStack: MutableList<Destinations>): Boolean {
    return myBackStack.size == 2
            && myBackStack[myBackStack.size - 2] == myBackStack.last()
            && myBackStack.last() == Destinations.getMainDestination()
}

fun goBackToPreviousDestination(
    myBackStack: MutableList<Destinations>,
    supportFragmentManager: FragmentManager,
    fragmentContainerID: Int,
    menu: Menu
) {
    val destination = myBackStack[myBackStack.size - 2]
    val currentFragment = supportFragmentManager.findFragmentByTag(myBackStack.last().getTag())
    val fragmentToOpen = supportFragmentManager.findFragmentByTag(destination.getTag())
    val transaction = supportFragmentManager.beginTransaction()
        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
    // Hide current if it exists
    if (currentFragment != null) {
        transaction.hide(currentFragment)
    }
    // Show the fragment to open
    if (fragmentToOpen != null) {
        transaction.show(fragmentToOpen)
    } else {
        // Get an instance if it somehow doesn't exists
        transaction.add(fragmentContainerID, destination.getFragment(), destination.getTag())
    }
    // Remove the destination from the custom BackStack
    myBackStack.removeAt(myBackStack.size - 1)
    // Manually check the destination on the bottom navigation bar
    menu.findItem(destination.getMenuItemID()).isChecked = true
    // Commit the transaction
    transaction.setPrimaryNavigationFragment(fragmentToOpen)
        .setReorderingAllowed(true)
        .commit()
}

fun openDestination(
    destination: Destinations,
    myBackStack: MutableList<Destinations>,
    supportFragmentManager: FragmentManager,
    fragmentContainerID: Int
) {
    val currentFragment = supportFragmentManager.findFragmentByTag(myBackStack.lastOrNull()?.getTag())
    val fragmentToOpen = supportFragmentManager.findFragmentByTag(destination.getTag())
    val transaction = supportFragmentManager.beginTransaction()
        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
    // Ignore reselection of shown fragment
    if (currentFragment == fragmentToOpen && fragmentToOpen != null) {
        return
    }
    // Hide the current fragment if it exists
    if (currentFragment != null) {
        transaction.hide(currentFragment)
    }
    // Show the fragment to Open
    if (fragmentToOpen == null) {
        // Get an instance if it's the first time the destination is selected
        transaction.add(fragmentContainerID, destination.getFragment(), destination.getTag())
    } else {
        transaction.show(fragmentToOpen)
    }
    // Remove previous instance of this destination if exists, avoid dropping first destination like YT app
    val position = myBackStack.drop(1).indexOf(destination)
    if (position != -1) {
        myBackStack.removeAt(position + 1)
    }
    // Add the destination to the custom BackStack
    myBackStack.add(destination)
    // Commit the transaction
    transaction.setPrimaryNavigationFragment(fragmentToOpen)
        .setReorderingAllowed(true)
        .commit()
}