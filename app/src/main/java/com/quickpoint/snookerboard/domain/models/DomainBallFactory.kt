package com.quickpoint.snookerboard.domain.models

import com.quickpoint.snookerboard.domain.utils.MatchConfig
import javax.inject.Inject

class BallFactory @Inject constructor(private val matchConfig: MatchConfig) {
    fun createBall(ballType: BallType, points: Int? = null) = when (ballType) {
        BallType.TYPE_NOBALL -> DomainBall.NOBALL(matchConfig.generateUniqueId(), points ?: 0, 0)
        BallType.TYPE_WHITE -> DomainBall.WHITE(matchConfig.generateUniqueId(), points ?: 0, 4 + matchConfig.foulModifier)
        BallType.TYPE_RED -> DomainBall.RED(matchConfig.generateUniqueId(), points ?: 1, 4 + matchConfig.foulModifier)
        BallType.TYPE_YELLOW -> DomainBall.YELLOW(matchConfig.generateUniqueId(), points ?: 2, 4 + matchConfig.foulModifier)
        BallType.TYPE_GREEN -> DomainBall.GREEN(matchConfig.generateUniqueId(), points ?: 3, 4 + matchConfig.foulModifier)
        BallType.TYPE_BROWN -> DomainBall.BROWN(matchConfig.generateUniqueId(), points ?: 4, 4 + matchConfig.foulModifier)
        BallType.TYPE_BLUE -> DomainBall.BLUE(matchConfig.generateUniqueId(), points ?: 5, 5 + matchConfig.foulModifier)
        BallType.TYPE_PINK -> DomainBall.PINK(matchConfig.generateUniqueId(), points ?: 6, 6 + matchConfig.foulModifier)
        BallType.TYPE_BLACK -> DomainBall.BLACK(matchConfig.generateUniqueId(), points ?: 7, 7 + matchConfig.foulModifier)
        BallType.TYPE_COLOR -> DomainBall.COLOR(matchConfig.generateUniqueId(), points ?: 1, 4 + matchConfig.foulModifier)
        BallType.TYPE_FREEBALL -> DomainBall.FREEBALL(0, points ?: 1, 4 + matchConfig.foulModifier)
        BallType.TYPE_FREEBALLAVAILABLE -> DomainBall.FREEBALLAVAILABLE(0, points ?: 1, 4 + matchConfig.foulModifier)
        BallType.TYPE_FREEBALLTOGGLE -> DomainBall.FREEBALLTOGGLE(0, points ?: 0, 0)
    }

    fun createBalls(vararg ballTypes: BallType): List<DomainBall> {
        val ballsList: MutableList<DomainBall> = mutableListOf()
        ballTypes.forEach {
            ballsList.add(createBall(it))
        }
        return ballsList
    }
}