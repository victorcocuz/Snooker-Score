package com.quickpoint.snookerboard.domain.models

import com.quickpoint.snookerboard.domain.models.BallType.TYPE_BLACK
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_BLUE
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_BROWN
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_COLOR
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_FREEBALL
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_GREEN
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_NOBALL
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_PINK
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_RED
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_WHITE
import com.quickpoint.snookerboard.domain.models.BallType.TYPE_YELLOW
import com.quickpoint.snookerboard.domain.utils.MatchConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class DomainBallManager @Inject constructor(
    private val matchConfig: MatchConfig,
    private val ballFactory: BallFactory
) {

    private val _ballsListFlow: MutableStateFlow<List<DomainBall>> = MutableStateFlow(emptyList())
    val ballsListFlow: StateFlow<List<DomainBall>> = _ballsListFlow.asStateFlow()

    fun onPot(potType: PotType, potAction: PotAction) {
        val crtBallsList = _ballsListFlow.value.toMutableList()
        crtBallsList.apply {
            when (potType) {
                PotType.TYPE_HIT, PotType.TYPE_REMOVE_COLOR, PotType.TYPE_FREE -> removeBalls(1)
                PotType.TYPE_ADDRED, PotType.TYPE_REMOVE_RED -> removeBalls(2)
                PotType.TYPE_SAFE, PotType.TYPE_MISS, PotType.TYPE_SAFE_MISS, PotType.TYPE_SNOOKER, PotType.TYPE_FOUL -> {
                    if (last() is DomainBall.COLOR && potAction != PotAction.RETAKE) removeBalls(1)
                    if (last() is DomainBall.FREEBALL) removeFreeBall()
                }
                PotType.TYPE_FREE_ACTIVE -> if (matchConfig.isFreeballEnabled) addFreeBall(1) else removeFreeBall()
                PotType.TYPE_LAST_BLACK_FOULED -> removeBalls(1)
                PotType.TYPE_RESPOT_BLACK -> addBalls(ballFactory.createBall(TYPE_BLACK))
                PotType.TYPE_FOUL_ATTEMPT -> {}
            }
        }
        updateBallsList(crtBallsList)
    }

    fun onUndo(potType: PotType, potAction: PotAction, breaksList: List<DomainBreak>) {
        val crtBallsList = _ballsListFlow.value.toMutableList()
        crtBallsList.apply {
            when (potType) {
                PotType.TYPE_HIT, PotType.TYPE_REMOVE_COLOR -> addNextBalls(1)
                PotType.TYPE_FREE -> addFreeBall(0)
                PotType.TYPE_ADDRED, PotType.TYPE_REMOVE_RED -> addNextBalls(2)
                PotType.TYPE_SAFE, PotType.TYPE_MISS, PotType.TYPE_SAFE_MISS, PotType.TYPE_SNOOKER, PotType.TYPE_FOUL -> {
                    when (breaksList.lastPotType()) {
                        PotType.TYPE_REMOVE_RED -> if (breaksList.lastBallTypeBeforeRemoveBall() == TYPE_RED) addNextBalls(1)
                        PotType.TYPE_HIT -> if (breaksList.lastBallType() == TYPE_RED && potAction != PotAction.RETAKE) addNextBalls(1)
                        PotType.TYPE_FREE -> if (!isInColorsPhase()) addNextBalls(1) // Adds a color to the ballstack
                        PotType.TYPE_FREE_ACTIVE -> addFreeBall(1)
                        else -> {}
                    }
                }
                PotType.TYPE_FREE_ACTIVE -> if (matchConfig.isFreeballEnabled) removeFreeBall() else addFreeBall(1)
                PotType.TYPE_LAST_BLACK_FOULED -> addNextBalls(1)
                PotType.TYPE_RESPOT_BLACK -> removeBalls(1)
                PotType.TYPE_FOUL_ATTEMPT -> {}
            }
        }
        updateBallsList(crtBallsList)
    }

    private fun addFreeBall(pol: Int) {
        val crtBallsList = _ballsListFlow.value.toMutableList()
        crtBallsList.apply {
            matchConfig.maxFramePoints += if (isInColorsPhase() || !wasPreviousBallColor()) {
                addBalls(ballFactory.createBall(TYPE_FREEBALL, points = last().points)) * pol
            } else {
                addBalls(
                    ballFactory.createBall(TYPE_COLOR),
                    ballFactory.createBall(TYPE_FREEBALL)
                ) * pol
            }
        }
        updateBallsList(crtBallsList)
    }

    fun resetBalls() {
        val crtBallsList = _ballsListFlow.value.toMutableList()
        crtBallsList.apply {
            clear()
            addNextBalls(matchConfig.availableReds * 2 + 7)
        }
        updateBallsList(crtBallsList)
    }

    private fun MutableList<DomainBall>.addBalls(vararg balls: DomainBall): Int {
        val points = when {
            isInColorsPhase() -> last().points
            !wasPreviousBallColor() -> 0
            else -> 8
        }
        for (ball in balls) {
            ball.ballId = matchConfig.generateUniqueId()
            add(ball)
        }
        return points
    }

    private fun List<DomainBall>.removeFreeBall() {
        matchConfig.maxFramePoints += removeBalls(if (isInColorsWithFreeBall()) 1 else 2)
    }


    private fun MutableList<DomainBall>.addNextBalls(number: Int) {
        repeat(number) {
            if (size >= 37) return // Early exit if the list is full
            addAll(ballFactory.createBalls(
                when (size) {
                    0 -> TYPE_WHITE
                    1 -> TYPE_BLACK
                    2 -> TYPE_PINK
                    3 -> TYPE_BLUE
                    4 -> TYPE_BROWN
                    5 -> TYPE_GREEN
                    6 -> TYPE_YELLOW
                    in (7..37).filter { it % 2 == 0 } -> TYPE_RED
                    in (7..37).filter { it % 2 == 1 } -> TYPE_COLOR
                    else -> TYPE_NOBALL
                })
            )
        }
    }

    private fun List<DomainBall>.removeBalls(times: Int): Int {
        val list = toMutableList()
        repeat(times) { list.removeLast() }
        return (if (times == 1) lastOrNull()?.points ?: 0 else 8) * -1
    }
    private fun List<DomainBall>.isInColorsPhase() = if (matchConfig.isFreeballEnabled) size <= 8 else size <= 7
    fun canRemoveColor(breaksList: List<DomainBreak>) =
        _ballsListFlow.value.isInColorsPhase() && breaksList.lastPotType() == PotType.TYPE_FREE


    fun isAddRedAvailable() = _ballsListFlow.value.isThisBallColorAndNotLast() && !matchConfig.isFreeballEnabled
    fun getBallsListForGameScreen(ballStack: List<DomainBall>) = when (ballStack.lastOrNull()) {
        is DomainBall.COLOR -> ballFactory.createBalls(TYPE_YELLOW, TYPE_GREEN, TYPE_BROWN, TYPE_BLUE, TYPE_PINK, TYPE_BLACK)
        is DomainBall.WHITE -> ballFactory.createBalls(TYPE_NOBALL)
        null -> emptyList()
        else -> listOf(ballStack.last())
    }
    fun getBallsListForFoulDialog(ballStack: List<DomainBall>) = when (ballStack.size) {
        0 -> emptyList()
        in (2..8) -> ballStack.filterBallsForFoulDialog()
        else -> ballFactory.createBalls(TYPE_RED, TYPE_YELLOW, TYPE_GREEN, TYPE_BROWN, TYPE_BLUE, TYPE_PINK, TYPE_BLACK, TYPE_WHITE)
    }
    fun updateBallsList(crtBallsList: List<DomainBall>) {
        _ballsListFlow.value = crtBallsList.toMutableList()
    }
    fun isLastBall() = _ballsListFlow.value.isLastBall()
    fun isActionTriggeringNoBallSnackMessage(potType: PotType) = isLastBall() && (potType in listOfPotTypesForNoBallSnackbar)
}