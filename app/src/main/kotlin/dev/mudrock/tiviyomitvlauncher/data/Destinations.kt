package dev.mudrock.tiviyomitvlauncher.data

import kotlinx.serialization.Serializable

object Destinations {
    @Serializable
    object Home

    @Serializable
    object Apps
}

val DefaultDestination = Destinations.Home
