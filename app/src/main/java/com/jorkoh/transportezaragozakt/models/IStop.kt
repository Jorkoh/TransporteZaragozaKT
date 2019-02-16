package com.jorkoh.transportezaragozakt.models

enum class StopType{
    BUS, TRAM
}

interface IStop {
    val title: String
    val destinations: List<IStopDestination>
}

interface IStopDestination {
    val line: String
    val destination : String
    val times : List<Int>
}