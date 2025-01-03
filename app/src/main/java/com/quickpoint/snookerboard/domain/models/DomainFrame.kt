package com.quickpoint.snookerboard.domain.models

import com.quickpoint.snookerboard.data.database.models.DbActionLog
import com.quickpoint.snookerboard.data.database.models.DbBall
import com.quickpoint.snookerboard.data.database.models.DbBreak
import com.quickpoint.snookerboard.data.database.models.DbFrame
import com.quickpoint.snookerboard.data.database.models.DbScore

// The DOMAIN Frame is a class containing all frame information
data class DomainFrame(
    val frameId: Long,
    val ballsList: List<DomainBall>, // Keep track of all DOMAIN Balls (i.e. a list of all balls potted, in order)
    val scoreList: List<DomainScore>, // Two element array to keep track of DOMAIN Score (i.e. overrides latest score)
    val breaksList: List<DomainBreak>, // Keep track of all DOMAIN Breaks (i.e. a list of all breaks)
    val actionLogs: List<DomainActionLog>, // Keep track of all actions for debug purposes
    val frameMax: Int, // Keep track of maximum remaining points
) {
    fun getTextInfo(): String {
        var text = "Id: $frameId "
        breaksList.forEach { breaks -> breaks.pots.forEach { pot -> text = text + pot.ball.ballType.toString() + ", " } }
        return text
    }
}


// CONVERTER method from DOMAIN frame to a list of DATABASE Frame
fun DomainFrame.asDbFrame(): DbFrame {
    return DbFrame(
        frameId = frameId,
        frameMax = frameMax
    )
}

// CONVERTER method from DOMAIN Frame a list of DATABASE Balls
fun DomainFrame.asDbBallStack(): List<DbBall> {
    return ballsList.map { ball ->
        DbBall(
            ballId = ball.ballId,
            frameId = frameId,
            ballValue = ball.getBallOrdinal(),
            ballPoints = ball.points,
            ballFoul = ball.foul
        )
    }
}

// CONVERTER method from DOMAIN frame to a list of DATABASE Score
fun DomainFrame.asDbCrtScore(): List<DbScore> {
    return scoreList.map { score ->
        DbScore(
            scoreId = score.scoreId,
            frameId = frameId,
            playerId = score.playerId,
            framePoints = score.framePoints,
            matchPoints = score.matchPoints,
            successShots = score.successShots,
            missedShots = score.missedShots,
            safetySuccessShots = score.safetySuccessShots,
            safetyMissedShots = score.safetyMissedShots,
            snookers = score.snookers,
            fouls = score.fouls,
            highestBreak = score.highestBreak,
            longShotsSuccess = score.longShotsSuccess,
            longShotsMissed = score.longShotsMissed,
            restShotsSuccess = score.longShotsSuccess,
            restShotsMissed = score.longShotsMissed,
            pointsWithNoReturn = score.pointsWithoutReturn
        )
    }
}

// CONVERTER method from DOMAIN Frame a list of DATABASE Breaks
fun DomainFrame.asDbBreaks(): List<DbBreak> {
    return breaksList.map {
        DbBreak(
            breakId = it.breakId,
            player = it.player,
            frameId = it.frameId,
            breakSize = it.breakSize,
            pointsWithoutReturn = it.pointsWithoutReturn
        )
    }
}

// CONVERTER method from DOMAIN Frame a list of DATABASE DbDebugFrameAction list
fun DomainFrame.asDbDebugFrameActions(): List<DbActionLog> {
    return actionLogs.map { actionLog ->
        DbActionLog(
            frameId = frameId,
            description = actionLog.description,
            potType = actionLog.potType,
            ballType = actionLog.ballType,
            ballPoints = actionLog.ballPoints,
            potAction = actionLog.potAction,
            player = actionLog.player,
            breakCount = actionLog.breakCount,
            ballsListLast = actionLog.ballsListLast,
            frameCount = actionLog.frameCount
        )
    }
}