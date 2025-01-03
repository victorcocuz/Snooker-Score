package com.quickpoint.snookerboard.domain.models

import com.quickpoint.snookerboard.core.utils.MatchAction
import com.quickpoint.snookerboard.domain.utils.MatchConfig
import com.quickpoint.snookerboard.domain.utils.getHandicap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.math.max

class DomainScoreManager @Inject constructor(
    private val matchConfig: MatchConfig,
) {

    private val _scoreListFlow = MutableStateFlow<List<DomainScore>>(emptyList())
    val scoreListFlow: StateFlow<List<DomainScore>> = _scoreListFlow.asStateFlow()

    fun resetFrame(matchAction: MatchAction) {
        _scoreListFlow.value = _scoreListFlow.value.toMutableList().apply {
            forEachIndexed { index, domainScore ->
                domainScore.resetFrame(index, matchAction)
            }
        }
    }

    private fun DomainScore.resetFrame(index: Int, matchAction: MatchAction) {
        if (matchAction != MatchAction.FRAME_RERACK) scoreId = matchConfig.generateUniqueId()
        playerId = index
        framePoints = getHandicap(matchConfig.handicapFrame, if (index == 0) -1 else 1)
        successShots = 0
        missedShots = 0
        safetySuccessShots = 0
        safetyMissedShots = 0
        snookers = 0
        fouls = 0
        highestBreak = 0
        longShotsSuccess = 0
        longShotsMissed = 0
        restShotsSuccess = 0
        restShotsMissed = 0
    }

    fun resetMatch() {
        _scoreListFlow.value = _scoreListFlow.value.toMutableList().apply {
            clear()
            (0 until 2).forEach {
                add(
                    DomainScore(
                        0,
                        0,
                        0,
                        0,
                        getHandicap(matchConfig.handicapMatch, if (it == 0) -1 else 1),
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0
                    )
                )
            }
        }
    }

    fun endFrame() {
        _scoreListFlow.value = _scoreListFlow.value.toMutableList().apply {
            if (matchConfig.counterRetake == 3) this[matchConfig.getOtherPlayer()].matchPoints += 1 // If a non-snooker shot was retaken 3 times game is lost by the crt player
            else this[frameWinner()].matchPoints += 1
            for (score in this) score.frameId =
                matchConfig.crtFrame // TEMP - Assign a frameId to later use to add frame info to DATABASE
            matchConfig.pointsWithoutReturn =
                if (this[0].pointsWithoutReturn > 0) this[0].pointsWithoutReturn * -1
                else this[1].pointsWithoutReturn
        }
    }

    fun calculatePoints(pot: DomainPot, potDirection: PotDirection, lastFoulSize: Int, highestBreak: Int) {
        val crtScoreList = _scoreListFlow.value.toMutableList()
        val points: Int
        val pol = if (potDirection == PotDirection.POT) 1 else -1

        when (pot.potType) { // Generic shots score
            PotType.TYPE_HIT, PotType.TYPE_FREE, PotType.TYPE_ADDRED -> {
                points = pot.ball.points
                crtScoreList[matchConfig.crtPlayer].framePoints += pol * points // Polarity is used to reverse score on undo
                pot.ball.points = points
                crtScoreList[matchConfig.crtPlayer].successShots += pol
            }

            PotType.TYPE_FOUL -> {
                points = if (pot.ball.ballType == BallType.TYPE_WHITE) max(lastFoulSize, 4) else pot.ball.foul
                pot.ball.foul = points
                crtScoreList[matchConfig.getOtherPlayer()].framePoints += pol * points
                crtScoreList[matchConfig.crtPlayer].missedShots += pol
                crtScoreList[matchConfig.crtPlayer].fouls += pol
            }

            PotType.TYPE_MISS -> crtScoreList[matchConfig.crtPlayer].missedShots += pol
            PotType.TYPE_SAFE -> crtScoreList[matchConfig.crtPlayer].safetySuccessShots += pol
            PotType.TYPE_SAFE_MISS -> crtScoreList[matchConfig.crtPlayer].safetyMissedShots += pol
            PotType.TYPE_SNOOKER -> crtScoreList[matchConfig.crtPlayer].snookers += pol
            else -> {}
        }
        when (pot.potType) { // Long shots and rest shots score
            PotType.TYPE_HIT, PotType.TYPE_FREE, PotType.TYPE_SAFE, PotType.TYPE_SNOOKER -> {
                if (pot.shotType in listOf(
                        ShotType.LONG_AND_REST,
                        ShotType.LONG
                    )
                ) crtScoreList[matchConfig.crtPlayer].longShotsSuccess += pol
                if (pot.shotType in listOf(
                        ShotType.LONG_AND_REST,
                        ShotType.REST
                    )
                ) crtScoreList[matchConfig.crtPlayer].restShotsSuccess += pol
            }

            PotType.TYPE_FOUL, PotType.TYPE_MISS, PotType.TYPE_SAFE_MISS -> {
                if (pot.shotType in listOf(
                        ShotType.LONG_AND_REST,
                        ShotType.LONG
                    )
                ) crtScoreList[matchConfig.crtPlayer].longShotsMissed += pol
                if (pot.shotType in listOf(
                        ShotType.LONG_AND_REST,
                        ShotType.REST
                    )
                ) crtScoreList[matchConfig.crtPlayer].restShotsMissed += pol
            }

            else -> {}
        }

        updateHighestBreak(highestBreak)
        matchConfig.updateCounterRetake(pot, potDirection)
        updatePointsWithoutReturn(pot, crtScoreList, potDirection)
    }

    private fun updateHighestBreak(maxBreak: Int) {
        val crtScore = _scoreListFlow.value
        crtScore[matchConfig.crtPlayer].highestBreak = maxBreak
        _scoreListFlow.value = crtScore
    }

    private fun updatePointsWithoutReturn(pot: DomainPot, crtScoreList: MutableList<DomainScore>, potDirection: PotDirection) {
        val pointsWithoutReturn = matchConfig.calculatePointsWithoutReturn(pot, potDirection)
        crtScoreList[matchConfig.crtPlayer].pointsWithoutReturn = if (pointsWithoutReturn >= 0) pointsWithoutReturn else 0
        crtScoreList[matchConfig.getOtherPlayer()].pointsWithoutReturn = if (pointsWithoutReturn >= 0) 0 else -pointsWithoutReturn
    }

    fun updateScoreList(newScoreList: List<DomainScore>) {
        _scoreListFlow.value = newScoreList.toMutableList()
    }

    fun isMatchEnding() =
        if (_scoreListFlow.value.isEmpty()) false else _scoreListFlow.value[_scoreListFlow.value.frameWinner()].matchPoints + 1 == matchConfig.availableFrames


}