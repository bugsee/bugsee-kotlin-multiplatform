package com.bugsee.kmp

import cocoapods.Bugsee.BugseeLogLevel

internal class BugseeIOSUtils {
    internal companion object {

        fun convertLogLevel(logLevel: com.bugsee.kmp.BugseeLogLevel): ULong {
            return when (logLevel) {
                com.bugsee.kmp.BugseeLogLevel.Error -> 1
                com.bugsee.kmp.BugseeLogLevel.Warning -> 2
                com.bugsee.kmp.BugseeLogLevel.Info -> 3
                com.bugsee.kmp.BugseeLogLevel.Debug -> 4
                com.bugsee.kmp.BugseeLogLevel.Verbose -> 5
            }.toULong()
        }

        fun convertLogLevel(logLevel: ULong): com.bugsee.kmp.BugseeLogLevel {
            return when (logLevel) {
                1uL -> com.bugsee.kmp.BugseeLogLevel.Error
                2uL -> com.bugsee.kmp.BugseeLogLevel.Warning
                3uL -> com.bugsee.kmp.BugseeLogLevel.Info
                4uL -> com.bugsee.kmp.BugseeLogLevel.Debug
                5uL -> com.bugsee.kmp.BugseeLogLevel.Verbose

                else -> com.bugsee.kmp.BugseeLogLevel.Info
            }
        }

        fun convertNetworkEventStage(stage: String): BugseeNetworkEventStage {
            return when (stage) {
                "before" -> BugseeNetworkEventStage.Before
                "complete" -> BugseeNetworkEventStage.Complete
                "cancel" -> BugseeNetworkEventStage.Cancel
                "error" -> BugseeNetworkEventStage.Errors

                else ->  BugseeNetworkEventStage.Before
            }
        }
    }
}