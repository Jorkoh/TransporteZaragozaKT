package com.jorkoh.transportezaragozakt

import agency.tango.materialintroscreen.MaterialIntroActivity
import agency.tango.materialintroscreen.fragments.SlideFragmentBuilder
import android.Manifest
import android.os.Bundle
import org.koin.androidx.viewmodel.ext.android.viewModel


class IntroActivity : MaterialIntroActivity() {

    private val introActivityVM: IntroActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSkipButtonVisible()
        enableLastSlideAlphaExitTransition(false)

        addSlide(
            SlideFragmentBuilder()
                .backgroundColor(R.color.splash_red)
                .buttonsColor(R.color.colorSecondaryZaragozaTransport)
                .image(R.drawable.ic_splash_text)
                .title(getString(R.string.slide_welcome_title))
                .description(getString(R.string.slide_welcome_description))
                .build()
        )

        addSlide(
            SlideFragmentBuilder()
                .backgroundColor(R.color.splash_red)
                .buttonsColor(R.color.colorSecondaryZaragozaTransport)
                .image(R.drawable.ic_map_black_24dp_animated)
                .possiblePermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                .title(getString(R.string.slide_find_title))
                .description(getString(R.string.slide_find_description))
                .build()
        )

        addSlide(
            SlideFragmentBuilder()
                .backgroundColor(R.color.splash_red)
                .buttonsColor(R.color.colorSecondaryZaragozaTransport)
                .image(R.drawable.ic_favorites_black_24dp_animated)
                .title(getString(R.string.slide_favorites_title))
                .description(getString(R.string.slide_favorites_description))
                .build()
        )

        addSlide(
            SlideFragmentBuilder()
                .backgroundColor(R.color.splash_red)
                .buttonsColor(R.color.colorSecondaryZaragozaTransport)
                .image(R.drawable.ic_alarm_black_24dp_animated)
                .title(getString(R.string.slide_reminder_title))
                .description(getString(R.string.slide_reminder_description))
                .build()
        )

        addSlide(
            SlideFragmentBuilder()
                .backgroundColor(R.color.splash_red)
                .buttonsColor(R.color.colorSecondaryZaragozaTransport)
                .image(R.drawable.ic_more_black_24dp_animated)
                .title(getString(R.string.slide_more_title))
                .description(getString(R.string.slide_more_description))
                .build()
        )
    }

    // Also called when intro is skipped by backing out from first slide
    override fun onLastSlidePassed() {
        super.onLastSlidePassed()
        introActivityVM.isFirstLaunch(false)
    }
}