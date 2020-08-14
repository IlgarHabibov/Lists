package com.precopia.david.lists.util

interface IUtilNightModeContract {
    fun isNightModeEnabled(): Boolean

    fun setDay()

    fun setNight()

    fun setFollowSystem()

    fun restore()


    enum class ThemeLabels(val label: String) {
        DAY("Day"),
        DARK("Dark"),
        FOLLOW_SYSTEM("System default")
    }

    enum class ThemeValues(val value: String) {
        DAY("0"),
        DARK("1"),
        FOLLOW_SYSTEM("2")
    }
}
