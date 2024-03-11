package com.quickpoint.snookerboard.domain.models

import com.quickpoint.snookerboard.domain.utils.MatchSettings
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DomainBallManager @Inject constructor(
    private val matchSettings: MatchSettings,
) {

    fun onPot(balls: MutableList<DomainBall>, potType: PotType, potAction: PotAction) {
        when (potType) {
            PotType.TYPE_HIT, PotType.TYPE_REMOVE_COLOR, PotType.TYPE_FREE -> balls.removeBalls(1)
            PotType.TYPE_ADDRED, PotType.TYPE_REMOVE_RED -> balls.removeBalls(2)
            PotType.TYPE_SAFE, PotType.TYPE_MISS, PotType.TYPE_SAFE_MISS, PotType.TYPE_SNOOKER, PotType.TYPE_FOUL -> {
                if (balls.last() is DomainBall.COLOR && potAction != PotAction.RETAKE) balls.removeBalls(1)
                if (balls.last() is DomainBall.FREEBALL) {
                    balls.removeFreeBall()
                }
            }

            PotType.TYPE_FREE_ACTIVE -> if (matchSettings.isFreeballEnabled) balls.addFreeBall(1) else balls.removeFreeBall()
            PotType.TYPE_LAST_BLACK_FOULED -> balls.removeBalls(1)
            PotType.TYPE_RESPOT_BLACK -> balls.addBalls(DomainBall.BLACK())
            PotType.TYPE_FOUL_ATTEMPT -> {}
        }
    }

    fun onUndo(balls: MutableList<DomainBall>, potType: PotType, potAction: PotAction, frameStack: MutableList<DomainBreak>) {
        when (potType) {
            PotType.TYPE_HIT, PotType.TYPE_REMOVE_COLOR -> balls.addNextBalls(1)
            PotType.TYPE_FREE -> balls.addFreeBall(0)
            PotType.TYPE_ADDRED, PotType.TYPE_REMOVE_RED -> balls.addNextBalls(2)
            PotType.TYPE_SAFE, PotType.TYPE_MISS, PotType.TYPE_SAFE_MISS, PotType.TYPE_SNOOKER, PotType.TYPE_FOUL -> {
                when (frameStack.lastPotType()) {
                    PotType.TYPE_REMOVE_RED -> if (frameStack.lastBallTypeBeforeRemoveBall() == BallType.TYPE_RED) balls.addNextBalls(1)
                    PotType.TYPE_HIT -> if (frameStack.lastBallType() == BallType.TYPE_RED && potAction != PotAction.RETAKE) balls.addNextBalls(1)
                    PotType.TYPE_FREE -> if (!isInColors(balls)) balls.addNextBalls(1) // Adds a color to the ballstack
                    PotType.TYPE_FREE_ACTIVE -> balls.addFreeBall(1)
                    else -> {}
                }
            }

            PotType.TYPE_FREE_ACTIVE -> if (matchSettings.isFreeballEnabled) balls.removeFreeBall() else balls.addFreeBall(1)
            PotType.TYPE_LAST_BLACK_FOULED -> balls.addNextBalls(1)
            PotType.TYPE_RESPOT_BLACK -> balls.removeBalls(1)
            PotType.TYPE_FOUL_ATTEMPT -> {}
        }
    }

    fun isInColors(balls: List<DomainBall>) = if (matchSettings.isFreeballEnabled) balls.size <= 8 else balls.size <= 7

    fun isAddRedAvailable(balls: List<DomainBall>) = balls.isThisBallColorAndNotLast() && !matchSettings.isFreeballEnabled

    fun availablePoints(balls: List<DomainBall>?): Int {
        if (balls == null) return 0
        val freeSize = (if (matchSettings.isFreeballEnabled) balls.size - 1 else balls.size)
        return if (freeSize <= 7) (-(8 - freeSize) * ((8 - freeSize) + 1) + 56) / 2 + (if (matchSettings.isFreeballEnabled) (9 - freeSize) else 0)
        else 27 + ((balls.size - 7) / 2) * 8 + (if (balls.size % 2 == 0) 7 else 0)
    }

    fun addBalls(ballStack: MutableList<DomainBall>, vararg balls: DomainBall): Int {
        val points = when {
            isInColors(ballStack) -> ballStack.last().points
            !ballStack.wasPreviousBallColor() -> 0
            else -> 8
        }
        for (ball in balls) {
            ball.ballId = MatchSettings.uniqueId
            ballStack.add(ball)
        }
        return points
    }

    fun addFreeBall(balls: MutableList<DomainBall>,pol: Int) {
        MatchSettings.maxFramePoints += if (isInColors(balls) || !balls.wasPreviousBallColor()) {
            addBalls(balls, DomainBall.FREEBALL(points = balls.last().points)) * pol
        } else {
            addBalls(balls, DomainBall.COLOR(), DomainBall.FREEBALL()) * pol
        }
    }

}