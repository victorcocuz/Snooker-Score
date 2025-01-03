package com.quickpoint.snookerboard.domain.models

import com.quickpoint.snookerboard.data.database.models.DbPot
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_FOUL
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_HIT

// The DOMAIN Break class is a list of balls potted in one visit (consecutive balls by one player until the other player takes over or the frame ends)
data class DomainBreak(
    val breakId: Long,
    val player: Int,
    val frameId: Long,
    val pots: MutableList<DomainPot>,
    var breakSize: Int,
    var pointsWithoutReturn: Int,
) {
    fun lastPot() = pots.lastOrNull()
    fun lastPotType() = lastPot()?.potType
    fun lastBall() = lastPot()?.ball
}

// CONVERTER method from DOMAIN Break to a list of DATABASE Pots
fun DomainBreak.asDbPots(breakId: Long): List<DbPot> {
    return pots.map { pot ->
        DbPot(
            potId = pot.potId,
            breakId = breakId,
            ballId = pot.ball.ballId,
            ballOrdinal = pot.ball.getBallOrdinal(),
            ballPoints = pot.ball.points,
            ballFoul = pot.ball.foul,
            potType = pot.potType.ordinal,
            potAction = pot.potAction.ordinal,
            shotType = pot.shotType.ordinal
        )
    }
}

fun List<DomainBreak>.isFrameInProgress() = isNotEmpty()
fun List<DomainBreak>.lastPot() = lastOrNull()?.lastPot()
fun List<DomainBreak>.lastPotType() = lastOrNull()?.lastPotType()
fun List<DomainBreak>.lastBall() = lastPot()?.ball
fun List<DomainBreak>.lastBallType() = lastBall()?.ballType
fun List<DomainBreak>.lastBallTypeBeforeRemoveBall() =
    flatMap { it.pots.asReversed() }.find { pot -> pot.potType == TYPE_HIT }?.ball?.ballType
fun DomainBreak.isLastBallFoul() = pots.lastOrNull()?.potType == TYPE_FOUL
fun List<DomainBreak>.filterBreaksByPotType(isAdvancedBreaksActive: Boolean): List<DomainBreak> {
    val showablePotTypes = if (isAdvancedBreaksActive) listOfAdvancedShowablePotTypes else listOfStandardShowablePotTypes
    return filter { showablePotTypes.contains(it.pots.last().potType) }.map { it.copy() }
}
fun DomainBreak.ballsList(crtPlayer: Int): List<DomainBall> {
    if (player != crtPlayer) return emptyList()
    val balls = mutableListOf<DomainBall>()
    pots.forEach { if (it.potType in listOfPotTypesPointGenerating) balls.add(it.ball) }
    return balls
}