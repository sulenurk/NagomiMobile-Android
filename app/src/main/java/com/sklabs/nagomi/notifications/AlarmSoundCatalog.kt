package com.sklabs.nagomi.notifications

import com.sklabs.nagomi.R

data class AlarmSoundOption(
    val key: String,
    val label: String,
    val resourceId: Int,
)

object AlarmSoundCatalog {
    val options = listOf(
        AlarmSoundOption("analog", "Analog", R.raw.analog),
        AlarmSoundOption("beep", "Beep", R.raw.beep),
        AlarmSoundOption("birdy", "Birdy", R.raw.birdy),
        AlarmSoundOption("buzz", "Buzz", R.raw.buzz),
        AlarmSoundOption("dance", "Dance", R.raw.dans),
        AlarmSoundOption("galaxy", "Galaxy", R.raw.galaxy),
    )

    fun resourceId(key: String): Int = options.firstOrNull { it.key == key }?.resourceId ?: R.raw.beep
}
