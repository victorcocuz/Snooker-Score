package com.quickpoint.snookerboard.domain.utils

import com.quickpoint.snookerboard.core.utils.MatchAction
import com.quickpoint.snookerboard.domain.models.DomainPot
import com.quickpoint.snookerboard.domain.models.PotAction
import com.quickpoint.snookerboard.domain.models.PotDirection
import com.quickpoint.snookerboard.domain.models.PotType

interface MatchConfig {
    var foulModifier: Int
    var matchState: MatchState
    var availableFrames: Int
    var availableReds: Int
    var startingPlayer: Int
    var handicapFrame: Int
    var handicapMatch: Int
    var crtPlayer: Int
    var maxFramePoints: Int
    var counterRetake: Int
    var pointsWithoutReturn: Int
    var crtFrame: Long
    var isFreeballEnabled: Boolean
    var isLongShotToggleEnabled: Boolean
    var isRestShotToggleEnabled: Boolean

    fun generateUniqueId(): Long
    fun getOtherPlayer(): Int
    fun handicapFrameExceedsLimit(key: String, value: Int): Boolean
    fun handicapMatchExceedsLimit(key: String, value: Int): Boolean
    fun updateSettings(key: String, value: Int)
    fun getCrtPlayerFromPotAction(potAction: PotAction): Int
    fun resetFrameAndGetFirstPlayer(matchAction: MatchAction)
    fun getAsText(): String
    fun formatAvailableFramesForDisplay(): String
    fun resetRules(): Int

    suspend fun loadMatchIfSaved()
    fun handlePotFreeballToggle(potType: PotType)
    fun handleUndoFreeballToggle(potType: PotType, lastPotType: PotType?)
    fun isVisibleBallFouledThreeTimes(): Boolean
    fun calculatePointsWithoutReturn(pot: DomainPot, potDirection: PotDirection): Int
    fun updateCounterRetake(pot: DomainPot? = null, potDirection: PotDirection)
}
