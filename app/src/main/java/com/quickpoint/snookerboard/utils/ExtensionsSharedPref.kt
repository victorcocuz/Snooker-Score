package com.quickpoint.snookerboard.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.quickpoint.snookerboard.R
import com.quickpoint.snookerboard.domain.DomainFreeBallInfo.FREEBALLINFO
import com.quickpoint.snookerboard.domain.DomainMatchInfo.RULES
import com.quickpoint.snookerboard.domain.DomainPlayer.PLAYER01
import com.quickpoint.snookerboard.domain.DomainPlayer.PLAYER02
import timber.log.Timber

// Shared preferences
fun Activity.getSharedPref(): SharedPreferences = application.getSharedPref()
fun Application.getSharedPref(): SharedPreferences = getSharedPreferences(
    getString(R.string.sp_file_key),
    Context.MODE_PRIVATE
)

fun SharedPreferences.savePref(application: Application) {
    edit().apply {
        application.apply {
            putInt(getString(R.string.sp_match_state), RULES.matchState.ordinal)
            putString(getString(R.string.sp_match_name_first_a), PLAYER01.firstName)
            putString(getString(R.string.sp_match_name_last_a), PLAYER01.lastName)
            putString(getString(R.string.sp_match_name_first_b), PLAYER02.firstName)
            putString(getString(R.string.sp_match_name_last_b), PLAYER02.lastName)
            putInt(getString(R.string.sp_match_frames), RULES.frames)
            putInt(getString(R.string.sp_match_reds), RULES.reds)
            putInt(getString(R.string.sp_match_foul), RULES.foul)
            putInt(getString(R.string.sp_match_first), RULES.first)
            putInt(getString(R.string.sp_match_crt_player), RULES.crtPlayer)
            putInt(getString(R.string.sp_match_frame_count), RULES.frameCount)
            putInt(getString(R.string.sp_match_frame_max), RULES.frameMax)
            putBoolean(getString(R.string.sp_match_freeball_visibility), FREEBALLINFO.isVisible)
            putBoolean(getString(R.string.sp_match_freeball_selection), FREEBALLINFO.isSelected)
            apply()
        }
        Timber.i("Add to sharedPref ${RULES.matchState}, ${PLAYER01.getPlayerText()} and ${PLAYER02.getPlayerText()}")
        Timber.i("Add to sharedPref ${RULES.getRulesText()}")
        Timber.i("Add to sharedPref freeball isVisible ${FREEBALLINFO.isVisible} and isSelected ${FREEBALLINFO.isSelected}")
    }
}

fun SharedPreferences.loadPref(application: Application) {
    application.resources.apply {
        RULES.getMatchStateFromOrdinal(getInt(getString(R.string.sp_match_state), 0))
        PLAYER01.firstName = getString(getString(R.string.sp_match_name_first_a), "")
        PLAYER01.lastName = getString(getString(R.string.sp_match_name_last_a), "")
        PLAYER02.firstName = getString(getString(R.string.sp_match_name_first_b), "")
        PLAYER02.lastName = getString(getString(R.string.sp_match_name_last_b), "")
        RULES.assignRules(
            getInt(getString(R.string.sp_match_frames), 3),
            getInt(getString(R.string.sp_match_reds), 15),
            getInt(getString(R.string.sp_match_foul), 0),
            getInt(getString(R.string.sp_match_first), 0),
            getInt(getString(R.string.sp_match_crt_player), 0),
            getInt(getString(R.string.sp_match_frame_count), 1),
            getInt(getString(R.string.sp_match_frame_max), 0)
        )
        FREEBALLINFO.assignFreeballInfo(
            getBoolean(getString(R.string.sp_match_freeball_visibility), false),
            getBoolean(getString(R.string.sp_match_freeball_selection), false)
        )
    }
    Timber.i("Get from sharedPref ${RULES.matchState}, ${PLAYER01.getPlayerText()} and ${PLAYER02.getPlayerText()}")
    Timber.i("Get from sharedPref ${RULES.getRulesText()}")
    Timber.i("Get from sharedPref freeball isVisible ${FREEBALLINFO.isVisible} and isSelected ${FREEBALLINFO.isSelected}")

}
