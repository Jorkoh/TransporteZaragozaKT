//package com.jorkoh.transportezaragozakt.navigation
//
//import android.view.Menu
//import androidx.fragment.app.FragmentManager
//import com.jorkoh.transportezaragozakt.R
//
//fun needsCustomBackHandling(
//    myBackStack: MutableList<Destinations>,
//    supportFragmentManager: FragmentManager
//): Boolean {
//    return supportFragmentManager.backStackEntryCount == 0 && myBackStack.count() > 1 && !isDoubleHome(
//        myBackStack
//    )
//}
//
//private fun isDoubleHome(myBackStack: MutableList<Destinations>): Boolean {
//    return myBackStack.size == 2
//            && myBackStack[myBackStack.size - 2] == myBackStack.last()
//            && myBackStack.last() == Destinations.getMainDestination()
//}
//
//fun goBackToPreviousDestination(
//    myBackStack: MutableList<Destinations>,
//    supportFragmentManager: FragmentManager,
//    fragmentContainerID: Int,
//    menu: Menu
//) {
//    val origin = myBackStack.last()
//    val destination = myBackStack[myBackStack.size - 2]
//    val currentFragment = supportFragmentManager.findFragmentByTag(origin.getTag())
//    val fragmentToOpen = supportFragmentManager.findFragmentByTag(destination.getTag())
//    val transaction = supportFragmentManager.beginTransaction()
//
//    // Animation
//    if(origin < destination){
//        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
//    }else{
//        transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
//    }
//
//    // Hide current if it exists
//    if (currentFragment != null) {
//        transaction.hide(currentFragment)
//    }
//    // Show the fragment to open
//    if (fragmentToOpen != null) {
//        transaction.show(fragmentToOpen)
//    } else {
//        // Get an instance if it somehow doesn't exists
//        transaction.add(fragmentContainerID, destination.getFragment(), destination.getTag())
//    }
//    // Remove the destination from the custom BackStack
//    myBackStack.removeAt(myBackStack.size - 1)
//    // Manually check the destination on the bottom navigation bar
//    menu.findItem(destination.getMenuItemID()).isChecked = true
//    // Commit the transaction
//    transaction.setPrimaryNavigationFragment(fragmentToOpen)
//        .setReorderingAllowed(true)
//        .commit()
//}
//
//fun openDestination(
//    destination: Destinations,
//    myBackStack: MutableList<Destinations>,
//    supportFragmentManager: FragmentManager,
//    fragmentContainerID: Int
//) {
//    val origin = myBackStack.lastOrNull()
//    val currentFragment = supportFragmentManager.findFragmentByTag(origin?.getTag())
//    val fragmentToOpen = supportFragmentManager.findFragmentByTag(destination.getTag())
//
//    // Ignore reselection of shown fragment
//    if (currentFragment == fragmentToOpen && fragmentToOpen != null) {
//        return
//    }
//
//    val transaction = supportFragmentManager.beginTransaction()
//
//    // Animation
//    if(origin != null) {
//        if (origin < destination) {
//            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
//        } else {
//            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
//        }
//    }else{
//        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
//    }
//
//
//    // Hide the current fragment if it exists
//    if (currentFragment != null) {
//        transaction.hide(currentFragment)
//    }
//    // Show the fragment to Open
//    if (fragmentToOpen == null) {
//        // Get an instance if it's the first time the destination is selected
//        transaction.add(fragmentContainerID, destination.getFragment(), destination.getTag())
//    } else {
//        transaction.show(fragmentToOpen)
//    }
//    // Remove previous instance of this destination if exists, avoid dropping first destination like YT app
//    val position = myBackStack.drop(1).indexOf(destination)
//    if (position != -1) {
//        myBackStack.removeAt(position + 1)
//    }
//    // Add the destination to the custom BackStack
//    myBackStack.add(destination)
//    // Commit the transaction
//    transaction.setPrimaryNavigationFragment(fragmentToOpen)
//        .setReorderingAllowed(true)
//        .commit()
//}