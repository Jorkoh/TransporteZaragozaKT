/*
 * Copyright (c) 2017,2018   Mathijs Lagerberg, Pixplicity BV
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pixplicity.generate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.TimeUnit;

/**
 * When your app has launched a couple of times, this class will ask to give your app a rating on
 * the Play Store. If the user does not want to rate your app and indicates a complaint, you have
 * the option to redirect them to a feedback link.
 * <p>
 * To use, call the following on every app start (or when appropriate):<br>
 * <code>
 * Rate mRate = new Rate.Builder(context)
 * .setTriggerCount(10)
 * .setMinimumInstallTime(TimeUnit.DAYS.toMillis(7))
 * .setMessage(R.string.my_message_text)
 * .setSnackBarParent(view)
 * .build();
 * mRate.count();
 * </code>
 * When it is a good time to show a rating request, call:
 * <code>
 * mRate.showRequest();
 * </code>
 * </p>
 */
public final class Rate {

    private static final String PREFS_NAME = "pirate";
    private static final String KEY_INT_LAUNCH_COUNT = "launch_count";
    private static final String KEY_LONG_LAUNCH_COUNT = "launch_count_l";
    private static final String KEY_LONG_SHOWN_COUNT = "shown_count";
    private static final String KEY_BOOL_ASKED = "asked";
    private static final String KEY_BOOL_SHOWN_TOO_MANY_TIMES = "shown_too_many_times";
    private static final String KEY_LONG_FIRST_LAUNCH = "first_launch";
    private static final int THEME_DARK = 0;
    private static final int THEME_LIGHT = 1;
    private static final int DEFAULT_COUNT = 6;
    private static final int DEFAULT_REPEAT_COUNT = 30;
    private static final int DEFAULT_MAX_SHOWN_TIMES = 4;

    private static final long DEFAULT_INSTALL_TIME = TimeUnit.DAYS.toMillis(5);

    private final SharedPreferences mPrefs;
    private final String mPackageName;
    private final Context mBuilderContext;
    private CharSequence mInitialQuestion, mTextPositive, mTextNegative, mTextCancel, mTextNever, mTextFeedback;
    private CharSequence mSecondaryQuestionPositive;
    private CharSequence mSecondaryQuestionNegative;
    private int mTriggerCount = DEFAULT_COUNT;
    private long mMinInstallTime = DEFAULT_INSTALL_TIME;
    private int mRepeatCount = DEFAULT_REPEAT_COUNT;
    private int mMaxShownTimes = DEFAULT_MAX_SHOWN_TIMES;
    private ViewGroup mParentView;
    private OnFeedbackListener mFeedbackAction;
    private boolean mSnackBarSwipeToDismiss = true;
    private String mStoreLink;

    private Rate(@NonNull Context context) {
        mBuilderContext = context;
        mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mPackageName = context.getPackageName();
        mInitialQuestion = context.getString(R.string.initial_question);
        mSecondaryQuestionPositive = context.getString(R.string.secondary_question_positive);
        mSecondaryQuestionNegative = context.getString(R.string.secondary_question_negative);
        mTextPositive = context.getString(R.string.button_yes);
        mTextCancel = context.getString(R.string.button_no);
    }

    /**
     * Use {@link #count()} instead
     *
     * @return the {@link Rate} instance
     */
    @Deprecated
    public Rate launched() {
        return count();
    }

    /**
     * Call this method whenever your app is launched to increase the launch counter. Or whenever
     * the user performs an action that indicates immersion.
     *
     * @return the {@link Rate} instance
     */
    @NonNull
    public Rate count() {
        return increment(false);
    }

    @NonNull
    private Rate increment(final boolean force) {
        Editor editor = mPrefs.edit();
        // Get current launch count
        long count = getCount();
        // Increment, but only when we're not on a launch point. Otherwise we could miss
        // it when .count and .showRequest calls are not called exactly alternated
        final boolean isAtLaunchPoint = getRemainingCount() == 0;
        if (force || !isAtLaunchPoint) {
            count++;
        }
        editor.putLong(KEY_LONG_LAUNCH_COUNT, count).apply();
        // Save first launch timestamp
        if (mPrefs.getLong(KEY_LONG_FIRST_LAUNCH, -1) == -1) {
            editor.putLong(KEY_LONG_FIRST_LAUNCH, System.currentTimeMillis());
        }
        editor.apply();
        return this;
    }

    private void increaseShownTimes(){
        Editor editor = mPrefs.edit();
        long count = mPrefs.getLong(KEY_LONG_SHOWN_COUNT, 0L);
        count++;
        editor.putLong(KEY_LONG_SHOWN_COUNT, count).apply();
        if(count >= mMaxShownTimes){
            editor.putBoolean(KEY_BOOL_SHOWN_TOO_MANY_TIMES, true);
        }
    }

    /**
     * Returns how often The Action has been performed, ever. This is usually the app launch event.
     *
     * @return Number of times the app was launched.
     */
    private long getCount() {
        long count = mPrefs.getLong(KEY_LONG_LAUNCH_COUNT, 0L);
        // For apps upgrading from the 1.1.6 version:
        if (count == 0) {
            count = mPrefs.getInt(KEY_INT_LAUNCH_COUNT, 0);
        }
        return count;
    }

    /**
     * Returns how many more times the trigger action should be performed before it triggers the
     * rating request. This can be either the first request or consequent requests after dismissing
     * previous ones. This method does NOT consider if the request will be shown at all, e.g. when
     * "don't ask again" was checked.
     * <p>
     * If this method returns `0` (zero), the next call to {@link #showRequest(View, int, Context)} will show the
     * dialog.
     * </p>
     *
     * @return Remaining count before the next request is triggered.
     */
    public long getRemainingCount() {
        long count = getCount();
        if (count < mTriggerCount) {
            return mTriggerCount - count;
        } else {
            return (mRepeatCount - ((count - mTriggerCount) % mRepeatCount)) % mRepeatCount;
        }
    }

    /**
     * Checks if the app has been launched often enough to ask for a rating, and shows the rating
     * request if so. The rating request can be a SnackBar (preferred) or a dialog.
     *
     * @return If the request is shown or not
     * @see Builder#setSnackBarParent(ViewGroup)
     */
    public boolean showRequest(View coordinator, int anchorId, Context context) {
        final boolean asked = mPrefs.getBoolean(KEY_BOOL_ASKED, false);
        final boolean shownTooManyTimes = mPrefs.getBoolean(KEY_BOOL_SHOWN_TOO_MANY_TIMES, false);
        final long firstLaunch = mPrefs.getLong(KEY_LONG_FIRST_LAUNCH, 0);
        final boolean shouldShowRequest =
                getRemainingCount() == 0
                        && !asked
                        && !shownTooManyTimes
                        && System.currentTimeMillis() > firstLaunch + mMinInstallTime;
        if (shouldShowRequest && canRateApp()) {
            showRatingRequest(coordinator, anchorId, context);
        }
        return shouldShowRequest;
    }

    /**
     * Creates an Intent to launch the proper store page. This does not guarantee the Intent can be
     * launched (i.e. that the Play Store is installed).
     *
     * @return The Intent to launch the store.
     */
    @NonNull
    private Intent getStoreIntent() {
        final Uri uri = Uri.parse(mStoreLink != null ? mStoreLink : ("market://details?id=" + mPackageName));
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    /**
     * Shows the rating request immediately. For testing.
     *
     * @return the {@link Rate} instance
     */
    @SuppressWarnings("UnusedReturnValue")
    @NonNull
    public Rate test(View coordinator, int anchorId, Context context) {
        showRatingRequest(coordinator, anchorId, context);
        return this;
    }

    /**
     * Resets all data saved by Gene-rate. This is not advised in production builds
     * as behavior against user preferences can occur.
     *
     * @return the {@link Rate} instance
     */
    @SuppressWarnings("unused")
    @NonNull
    public Rate reset() {
        mPrefs.edit().clear().apply();
        return this;
    }

    private void showRatingRequest(View coordinator, int anchorId, Context context) {
        increment(true);
        increaseShownTimes();
        if (coordinator != null) {
            showInitialQuestionSnackbar(coordinator, anchorId, context);
        }
    }

    private void showInitialQuestionSnackbar(View coordinator, int anchorId, Context context) {
        final Snackbar snackbar = Snackbar.make(
                coordinator,
                mInitialQuestion,
                mSnackBarSwipeToDismiss ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_LONG
        );

        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        CoordinatorLayout.LayoutParams snackbarLayoutParams = (CoordinatorLayout.LayoutParams) snackbar.getView().getLayoutParams();
        snackbarLayoutParams.setAnchorId(anchorId);
        snackbarLayoutParams.anchorGravity = Gravity.TOP;
        snackbarLayoutParams.gravity = Gravity.TOP;
        snackbar.getView().setLayoutParams(snackbarLayoutParams);

        TextView textView = layout.findViewById(R.id.snackbar_text);
        textView.setVisibility(View.INVISIBLE);

        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        int layoutId = R.layout.in_snackbar;
        @SuppressLint("InflateParams")
        View snackView = inflater.inflate(layoutId, null);
        if (snackView.getBackground() != null) {
            int backgroundColor = ((ColorDrawable) snackView.getBackground()).getColor();
            layout.setBackgroundColor(backgroundColor);
        }

        TextView tvMessage = snackView.findViewById(R.id.text);
        tvMessage.setText(mInitialQuestion);
        final Button btPositive = snackView.findViewById(R.id.bt_yes);
        final Button btNegative = snackView.findViewById(R.id.bt_no);

        snackView.findViewById(R.id.tv_swipe).setVisibility(
                mSnackBarSwipeToDismiss ? View.VISIBLE : View.GONE);

        btPositive.setOnClickListener(v -> showSecondaryQuestionPostiveSnackbar(snackbar, context));
        btNegative.setOnClickListener(v -> {
            showSecondaryQuestionNegativeSnackbar(snackbar, context);
            saveAsked();
        });

        layout.addView(snackView, 0);
        snackbar.show();
    }

    private void showSecondaryQuestionPostiveSnackbar(Snackbar snackbar, Context context) {
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        View snackView = layout.getRootView();

        TextView tvMessage = snackView.findViewById(R.id.text);
        tvMessage.setText(mSecondaryQuestionPositive);

        final Button btPositive = snackView.findViewById(R.id.bt_yes);
        final Button btNegative = snackView.findViewById(R.id.bt_no);
        final Button btLater = snackView.findViewById(R.id.bt_later);

        btLater.setVisibility(View.VISIBLE);

        btPositive.setOnClickListener(v -> {
            openPlayStore(context);
            snackbar.dismiss();
            saveAsked();
        });
        btNegative.setOnClickListener(v -> {
            snackbar.dismiss();
            saveAsked();
        });
        btLater.setOnClickListener(v -> snackbar.dismiss());
    }

    private void showSecondaryQuestionNegativeSnackbar(Snackbar snackbar, Context context) {
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        View snackView = layout.getRootView();

        TextView tvMessage = snackView.findViewById(R.id.text);
        tvMessage.setText(mSecondaryQuestionNegative);

        final Button btPositive = snackView.findViewById(R.id.bt_yes);
        final Button btNegative = snackView.findViewById(R.id.bt_no);

        btPositive.setOnClickListener(v -> {
            mFeedbackAction.onFeedbackTapped();
            snackbar.dismiss();
        });
        btNegative.setOnClickListener(v -> snackbar.dismiss());
    }

    private void openPlayStore(Context context) {
        final Intent intent = getStoreIntent();
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    /**
     * Checks if the app can be rated, i.e. if the store Intent can be launched, i.e. if the Play
     * Store is installed.
     *
     * @return if the app can be rated
     * @see #getStoreIntent()
     */
    private boolean canRateApp() {
        return canOpenIntent(getStoreIntent());
    }

    /**
     * Checks if the system or any 3rd party app can handle the Intent
     *
     * @param intent the Intent
     * @return if the Intent can be handled by the system
     */
    private boolean canOpenIntent(@NonNull Intent intent) {
        return mBuilderContext
                .getPackageManager()
                .queryIntentActivities(intent, 0)
                .size() > 0;
    }

    private void saveAsked() {
        mPrefs.edit().putBoolean(KEY_BOOL_ASKED, true).apply();
    }

    @IntDef({THEME_LIGHT, THEME_DARK})
    @interface Theme {

    }

    @SuppressWarnings({"unused", "WeakerAccess", "SameParameterValue"})
    public static class Builder {

        private final Rate mRate;

        public Builder(@NonNull Context context) {
            mRate = new Rate(context);
        }

        /**
         * Set number of times {@link #count()} should be called before triggering the rating
         * request
         *
         * @param count Number of times (inclusive) to call {@link #count()} before rating
         *              request should show. Defaults to {@link #DEFAULT_COUNT}
         * @return The current {@link Builder}
         */
        @NonNull
        public Builder setTriggerCount(int count) {
            mRate.mTriggerCount = count;
            return this;
        }

        /**
         * Set amount of time the app should be installed before asking for a rating. Defaults to 5
         * days.
         *
         * @param millis Amount of time in milliseconds the app should be installed before asking a
         *               rating.
         * @return The current {@link Builder}
         */
        @NonNull
        public Builder setMinimumInstallTime(int millis) {
            mRate.mMinInstallTime = millis;
            return this;
        }

        /**
         * Sets the repeat count to bother the user again if "don't ask again" was checked.
         *
         * @param repeatCount Integer how often rate will wait if "don't ask again" was checked
         *                    (default 30).
         * @return The current {@link Builder}
         */
        @NonNull
        public Builder setRepeatCount(int repeatCount) {
            mRate.mRepeatCount = repeatCount;
            return this;
        }


        /**
         * Sets the text to show in the rating request on the positive button.
         *
         * @param message The text on the positive button
         * @return The current {@link Builder}
         * @see #setPositiveButton(int)
         */
        @NonNull
        public Builder setPositiveButton(@Nullable CharSequence message) {
            mRate.mTextPositive = message;
            return this;
        }

        /**
         * Sets the text to show in the rating request on the positive button.
         *
         * @param resId The text on the positive button
         * @return The current {@link Builder}
         * @see #setPositiveButton(CharSequence)
         */
        @NonNull
        public Builder setPositiveButton(@StringRes int resId) {
            return setPositiveButton(mRate.mBuilderContext.getString(resId));
        }

        /**
         * Sets the text to show in the rating request on the negative button.
         *
         * @param message The text on the negative button
         * @return The current {@link Builder}
         * @see #setNegativeButton(int)
         */
        @NonNull
        public Builder setNegativeButton(@Nullable CharSequence message) {
            mRate.mTextNegative = message;
            return this;
        }

        /**
         * Sets the text to show in the rating request on the negative button.
         *
         * @param resId The text on the negative button
         * @return The current {@link Builder}
         * @see #setNegativeButton(CharSequence)
         */
        @NonNull
        public Builder setNegativeButton(@StringRes int resId) {
            return setNegativeButton(mRate.mBuilderContext.getString(resId));
        }

        /**
         * Sets the text to show in the rating request on the cancel button.
         * Note that this will not be used when using a SnackBar.
         *
         * @param message The text on the cancel button
         * @return The current {@link Builder}
         * @see #setSnackBarParent(ViewGroup)
         * @see #setCancelButton(int)
         */
        @NonNull
        public Builder setCancelButton(@Nullable CharSequence message) {
            mRate.mTextCancel = message;
            return this;
        }

        /**
         * Sets the text to show in the rating request on the cancel button.
         * Note that this will not be used when using a SnackBar.
         *
         * @param resId The text on the cancel button
         * @return The current {@link Builder}
         * @see #setSnackBarParent(ViewGroup)
         * @see #setCancelButton(CharSequence)
         */
        @NonNull
        public Builder setCancelButton(@StringRes int resId) {
            return setCancelButton(mRate.mBuilderContext.getString(resId));
        }

        /**
         * Sets the text to show in the rating request on the checkbox.
         *
         * @param message The text on the checkbox
         * @return The current {@link Builder}
         */
        @NonNull
        public Builder setNeverAgainText(@Nullable CharSequence message) {
            mRate.mTextNever = message;
            return this;
        }

        /**
         * Sets the text to show in the rating request on the checkbox.
         *
         * @param resId The text on the checkbox
         * @return The current {@link Builder}
         */
        @NonNull
        public Builder setNeverAgainText(@StringRes int resId) {
            return setNeverAgainText(mRate.mBuilderContext.getString(resId));
        }

        /**
         * Sets the text to show in the feedback link.
         *
         * @param message The text in the link
         * @return The current {@link Builder}
         */
        @NonNull
        public Builder setFeedbackText(@Nullable CharSequence message) {
            mRate.mTextFeedback = message;
            return this;
        }

        /**
         * Sets the text to show in the feedback link.
         *
         * @param resId The text in the link
         * @return The current {@link Builder}
         */
        @NonNull
        public Builder setFeedbackText(@StringRes int resId) {
            return setFeedbackText(mRate.mBuilderContext.getString(resId));
        }

        /**
         * Sets the Uri to open when the user clicks the feedback button.
         * This can use the scheme `mailto:`, `tel:`, `geo:`, `https:`, etc.
         *
         * @param uri The Uri to open, or {@code null} to hide the feedback button
         * @return The current {@link Builder}
         * @see #setFeedbackAction(OnFeedbackListener)
         */
        @NonNull
        public Builder setFeedbackAction(@Nullable final Uri uri) {
            if (uri == null) {
                mRate.mFeedbackAction = null;
            } else {
                mRate.mFeedbackAction = new OnFeedbackAdapter() {

                    @Override
                    public void onFeedbackTapped() {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (mRate.canOpenIntent(intent)) {
                            mRate.mBuilderContext.startActivity(intent);
                        }
                    }
                };
            }
            return this;
        }

        /**
         * Sets the action to perform when the user clicks the feedback button.
         *
         * @param action Callback when the user taps the feedback button, or {@code null} to hide
         *               the feedback button
         * @return The current {@link Builder}
         * @see #setFeedbackAction(Uri)
         */
        @NonNull
        public Builder setFeedbackAction(@Nullable OnFeedbackListener action) {
            mRate.mFeedbackAction = action;
            return this;
        }

        /**
         * Sets the parent view for a Snackbar. This enables the use of a Snackbar for the rating
         * request instead of the default dialog.
         *
         * @param parent The parent view to put the Snackbar in, or {@code null} to disable the
         *               Snackbar
         * @return The current {@link Builder}
         */
        @NonNull
        public Builder setSnackBarParent(@Nullable ViewGroup parent) {
            mRate.mParentView = parent;
            return this;
        }

        /**
         * Sets the destination rate link if not Google Play.
         *
         * @param rateDestinationStore The destination link
         *                             (i.e. "amzn://apps/android?p=com.pixplicity.generate" ).
         *                             Keeps default Google Play store
         *                             as destination if rateDestinationStore is {@code null} or
         *                             empty.
         * @return The current {@link Builder}
         */
        public Builder setRateDestinationStore(String rateDestinationStore) {
            if (!TextUtils.isEmpty(rateDestinationStore)) {
                mRate.mStoreLink = rateDestinationStore;
            }
            return this;
        }

        /**
         * Shows or hides the 'swipe to dismiss' notion in the Snackbar. When disabled, the
         * Snackbar will automatically hide after a view seconds. When enabled, the Snackbar will
         * show indefinitively until dismissed by the user. <strong>Note that the
         * Snackbar can only be swiped when one of the parent views is a
         * {@code CoordinatorLayout}!</strong> Also, <strong>toggling this does not change
         * if the Snackbar can actually be swiped to dismiss!</strong>
         *
         * @param visible Show/hide the 'swipe to dismiss' text, and disable/enable auto-hide.
         *                Default is {code true}.
         * @return The current {@link Builder}
         */
        @NonNull
        public Builder setSwipeToDismissVisible(boolean visible) {
            mRate.mSnackBarSwipeToDismiss = visible;
            return this;
        }

        /**
         * Build the {@link Rate} instance
         *
         * @return a new Rate instance as configured by the current {@link Builder}
         */
        @NonNull
        public Rate build() {
            return mRate;
        }
    }
}
