package com.quickpoint.snookerboard.ui.navigation

import com.quickpoint.snookerboard.core.utils.Constants
import com.quickpoint.snookerboard.domain.utils.MatchState
import com.quickpoint.snookerboard.domain.utils.MatchState.GAME_IN_PROGRESS
import com.quickpoint.snookerboard.domain.utils.MatchState.RULES_IDLE
import com.quickpoint.snookerboard.ui.navigation.Screen.DrawerAbout
import com.quickpoint.snookerboard.ui.navigation.Screen.DrawerImprove
import com.quickpoint.snookerboard.ui.navigation.Screen.DrawerRules
import com.quickpoint.snookerboard.ui.navigation.Screen.DrawerSettings
import com.quickpoint.snookerboard.ui.navigation.Screen.DrawerSupport
import com.quickpoint.snookerboard.ui.navigation.Screen.Game
import com.quickpoint.snookerboard.ui.navigation.Screen.Rules
import com.quickpoint.snookerboard.ui.navigation.Screen.Summary

sealed class Screen(val route: String) {
    data object Main: Screen(Constants.ID_SCREEN_MAIN)

    data object Rules: Screen(Constants.ID_SCREEN_RULES)
    data object Game: Screen(Constants.ID_SCREEN_GAME)
    data object Summary: Screen(Constants.ID_SCREEN_SUMMARY)

    data object DrawerAbout: Screen(Constants.ID_SCREEN_ABOUT)
    data object DrawerImprove: Screen(Constants.ID_SCREEN_DRAWER_IMPROVE)
    data object DrawerRules: Screen(Constants.ID_SCREEN_DRAWER_RULES)
    data object DrawerSettings: Screen(Constants.ID_SCREEN_DRAWER_SETTINGS)
    data object DrawerSupport: Screen(Constants.ID_SCREEN_DRAWER_SUPPORT)
}

fun String?.isDrawerRoute() = this in listOf(
    DrawerAbout.route,
    DrawerImprove.route,
    DrawerRules.route,
    DrawerSettings.route,
    DrawerSupport.route
)

fun getRouteFromMatchState(matchState: MatchState) = when (matchState) {
    RULES_IDLE -> Rules.route
    GAME_IN_PROGRESS -> Game.route
    else -> Summary.route
}