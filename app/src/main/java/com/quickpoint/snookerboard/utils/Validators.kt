package com.quickpoint.snookerboard.utils

import androidx.compose.runtime.MutableState

// Rules Fragment
fun Map<String, MutableState<String>>.isNameIncomplete(): Boolean {
    forEach { playerName ->
        if (playerName.value.value == "") return true
    }
    return false
}
