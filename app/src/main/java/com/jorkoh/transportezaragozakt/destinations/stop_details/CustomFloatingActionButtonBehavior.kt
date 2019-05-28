package com.jorkoh.transportezaragozakt.destinations.stop_details

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import androidx.core.view.ViewCompat.setTranslationY
import android.R.attr.dependency
import android.opengl.ETC1.getHeight
import android.util.Log
import androidx.core.view.ViewCompat.getTranslationY



class CustomFloatingActionButtonBehavior : CoordinatorLayout.Behavior<View>(){
//    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
//        Log.d("TESTING STUFF", "layoutDependsOn")
//        return dependency is Snackbar.SnackbarLayout
//    }
//
//    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
//        Log.d("TESTING STUFF", "onDependentViewChanged")
//        val translationY = Math.min(0f, dependency.translationY - dependency.height)
//        child.translationY = translationY
//        return true
//    }
}