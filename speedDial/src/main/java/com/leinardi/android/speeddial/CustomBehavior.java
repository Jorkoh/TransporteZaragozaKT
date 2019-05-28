package com.leinardi.android.speeddial;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;

@SuppressWarnings({"unused", "WeakerAccess"})
public class CustomBehavior extends CoordinatorLayout.Behavior<SpeedDialView>{
    public CustomBehavior() {
    }

    public CustomBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull SpeedDialView child, @NonNull View dependency) {
        Log.d("TESTING STUFF", "layoutDependsOn");
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull SpeedDialView child, @NonNull View dependency) {
        Log.d("TESTING STUFF", "onDependentViewChanged");
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);
        return true;
    }
}