package com.quickpoint.snookerboard.domain.models

import kotlin.math.abs

// DOMAIN Player Score
data class DomainScore(
    var scoreId: Long,
    var frameId: Long,
    var playerId: Int,
    var framePoints: Int,
    var matchPoints: Int,
    var successShots: Int,
    var missedShots: Int,
    var safetySuccessShots: Int,
    var safetyMissedShots: Int,
    var snookers: Int,
    var fouls: Int,
    var highestBreak: Int,
    var longShotsSuccess: Int,
    var longShotsMissed: Int,
    var restShotsSuccess: Int,
    var restShotsMissed: Int,
    var pointsWithoutReturn: Int,
)

val emptyDomainScore = DomainScore(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1)

// Checker Methods
fun DomainScore.cumulatedValues() = framePoints + matchPoints + successShots + missedShots + safetyMissedShots + safetyMissedShots + snookers + fouls + highestBreak
fun List<DomainScore>.frameScoreDiff() = if (isEmpty()) 0 else abs((this[0].framePoints) - (this[1].framePoints))
fun List<DomainScore>.isFrameEqual() = if (isEmpty()) false else this[0].framePoints == this[1].framePoints
fun List<DomainScore>.isMatchEqual() = if (isEmpty()) false else this[0].matchPoints == this[1].matchPoints
fun List<DomainScore>.isFrameAndMatchEqual() = isFrameEqual() && isMatchEqual()
fun List<DomainScore>.isNoFrameFinished() = if (isEmpty()) false else this[0].matchPoints + this[1].matchPoints == 0
fun List<DomainScore>.frameWinner() = if (isEmpty()) 0 else if (this[0].framePoints > this[1].framePoints) 0 else 1
fun List<DomainScore>.isFrameWinResultingMatchTie() = if (isEmpty()) false else this[frameWinner()].matchPoints + 1 == this[1 - frameWinner()].matchPoints
fun List<DomainScore>.isMatchInProgress() = if (isEmpty()) false else (this[0].cumulatedValues() + this[1].cumulatedValues()) > 0
