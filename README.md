<h1 align="center"> Zaragoza Transport </h1> <br>
<p align="center">
    <img alt="Zaragoza Transport" title="Zaragoza Transport" src="/readme-resources/rounded_app_logo.png" width="256">
</p>

<p align="center">
  Zaragoza bus and tram in your pocket. Built with Kotlin and ❤️
</p>

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.jorkoh.transportezaragozakt">
    <img alt="Get it on Google Play" title="Google Play" src="/readme-resources/google_play_badge.png" width="140">
  </a>
</p>

## Table of Contents

- [Introduction](#introduction)
- [Technologies](#technologies)
- [Features](#features)
- [Feedback and Contributions](#feedback-and-contributions)
- [Acknowledgments](#acknowledgments)
- [License](#license)

## Introduction

Real-time Zaragoza bus and tram imformation. Built with Kotlin following modern best practices, Transporte Zaragoza aims to be the most intuitive and feature-rich alternative on Google Play.

<p align="center">
    <b>🚧 This app is WIP. Test coverage, architecture and features are not final 🚧</b>
    </br>
    </br>
    <img alt="Zaragoza Transport" title="Zaragoza Transport" src="/readme-resources/bottom_bar_animations.gif" height="50">
</p>

## Technologies

Some of the technologies used to develop Transporte Zaragoza:

* [Kotlin](https://kotlinlang.org/)\*, Google’s preferred language for Android app development.
* [LiveData](https://developer.android.com/topic/libraries/architecture/livedata), lifecycle-aware observables.
* [Room](https://developer.android.com/topic/libraries/architecture/room), abstraction layer over SQLite.
* [Retrofit](https://square.github.io/retrofit/) + [Moshi](https://github.com/square/moshi), interaction with [the API](https://www.zaragoza.es/sede/portal/datos-abiertos/api) that provides bus and tram data.
* [Koin](https://github.com/InsertKoinIO/koin), dependency injection for Kotlin.
* [Heroku](https://heroku.com/) + [Parse](https://parseplatform.org/) + [MongoDB](https://www.mongodb.com/), custom backend to support automatic app data updates outside of Google Play.
* [JUnit](https://junit.org/junit4/) + [Mockito](https://site.mockito.org/), local and instrumented unit testing.
* [Espresso](https://developer.android.com/training/testing/espresso), E2E UI testing.
* [Firebase Analytics and Crashlytics](https://firebase.google.com/), app usage and crash monitoring.
* [LeakCanary](https://square.github.io/leakcanary/), memory leak detection.
* [Lottie](https://airbnb.design/lottie/), After Effects animations rendered natively.
* [ShapeShifter](https://github.com/alexjlockwood/ShapeShifter), Animated Vector Drawable animations.

\* <sub>The app itself is 100% Kotlin code. Some of the modified open source libraries included as local modules are written in Java.</sub>
## Features

A few of the things you can do with Transporte Zaragoza:

* Get tram and bus **real-time arrival times**
* Find any stop quickly with the **search** function
* Move around with the **map**
* Add **favorite stops** for easier access
* Setup **notifications** to receive arrival times automatically
* Checkout tram and bus **lines** and destinations clearly
* Make the app yours by choosing between many **themes**
* Create **shortcuts** for your home screen
* Scan bus stops **QR codes**

<p align="center">
  <img src = "/readme-resources/features1.png" width=800>
</p>

<p align="center">
  <img src = "/readme-resources/features2.png" width=800>
</p>

## Feedback and Contributions

Feel free to [ask me anything](mailto:jorge@jorkoh.com) or [file an issue](https://github.com/Jorkoh/TransporteZaragozaKT/issues/new) if you find any problem. Feature requests are always welcome. 

Transporte Zaragoza is first and foremost a personal project to improve my own skillset and reflect my current abilities. This is why, for now, extensive pull requests are discouraged. 

## Acknowledgments

Some extra stuff that made this project a better experience:

* [FAB Speed Dial](https://github.com/leinardi/FloatingActionButtonSpeedDial). Implementation of the [Material Design speed dial](https://material.io/design/components/buttons-floating-action-button.html#types-of-transitions) used in the stop information destination. Created by [Roberto Leinardi](https://github.com/leinardi) and modified by me to support theming.
* [Dresscode](https://github.com/Daio-io/dresscode). Tiny convenience library that allows theme changing on runtime. Created by [Dai Williams](https://github.com/Daio-io) and modified by me to work with [Cyanea](https://github.com/jaredrummler/Cyanea) theme picker.
* [Material Dialogs](https://github.com/afollestad/material-dialogs). All kinds of dialogs made easy thanks to [Aidan Follestad](https://github.com/afollestad).
* [Quick Permissions](https://github.com/QuickPermissions/QuickPermissions). Handling runtime permissions with less boilerplate by [Kirtan403](https://github.com/kirtan403). Modified by me to use Material Dialogs and other small stuff.
* [ZXing Android Embedded](https://github.com/journeyapps/zxing-android-embedded). ZXing QR scanning capabilities without extra downloads.
* [Material Intro Screen](https://github.com/TangoAgency/material-intro-screen). Welcoming slides to introduce the user to the app features. Modified by me to support animated vector drawables.
* [Gene-rate](https://github.com/Pixplicity/gene-rate). Asks for ratings without interrupting the user's flow after conditions are met. Heavily modified by me with ideas from other libraries like [Integrated Rating Request](https://github.com/mediavrog/integrated-rating-request).
* Multiple Icons by [Freepik](https://www.freepik.com/) from [flaticon.com](https://www.flaticon.com).

## License

This project is licensed under the terms of the **CC BY-NC 4.0 License**. This means, among other things, that you **can not use this project commercially**. Please read the [full license terms](https://creativecommons.org/licenses/by-nc/4.0/legalcode) if you have any doubts.

