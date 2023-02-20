package com.quickpoint.snookerboard.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.quickpoint.snookerboard.database.SnookerDatabase
import com.quickpoint.snookerboard.database.models.DbFrameWithScoreAndBreakWithPotsAndBallStack
import com.quickpoint.snookerboard.database.models.asDomain
import com.quickpoint.snookerboard.database.models.asDomainPair
import com.quickpoint.snookerboard.domain.DomainActionLog
import com.quickpoint.snookerboard.domain.DomainFrame
import com.quickpoint.snookerboard.domain.DomainScore
import com.quickpoint.snookerboard.domain.objects.MatchSettings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class SnookerRepository constructor(database: SnookerDatabase) {

    private val daoDbActionLog = database.daoDbActionLog
    private val daoDbBall = database.daoDbBall
    private val daoDbBreak = database.daoDbBreak
    private val daoDbFrame = database.daoDbFrame
    private val daoDbPot = database.daoDbPot
    private val daoDbScore = database.daoDbScore

    suspend fun getTotals(playerId: Int) = withContext(Dispatchers.IO) {
        DomainScore(
            -2,
            -2,
            playerId,
            daoDbScore.getSumOfFramePoints(playerId),
            daoDbScore.getMaxMatchPoints(playerId),
            daoDbScore.getSumOfSuccessShots(playerId),
            daoDbScore.getSumOfMissedShots(playerId),
            daoDbScore.getSumOfSafetySuccessShots(playerId),
            daoDbScore.getSumOfSafetyMissedShots(playerId),
            daoDbScore.getSumOfSnookers(playerId),
            daoDbScore.getSumOfFouls(playerId),
            daoDbScore.getMaxBreak(playerId),
            daoDbScore.getSumOfLongShotsSuccess(playerId),
            daoDbScore.getSumOfLongShotsMissed(playerId),
            daoDbScore.getSumOfRestShotsSuccess(playerId),
            daoDbScore.getSumOfRestShotsMissed(playerId),
            daoDbScore.getMaxPointsWithNoReturn(playerId)
        )
    }

    suspend fun getCrtFrame(): DbFrameWithScoreAndBreakWithPotsAndBallStack? {
        return withContext(Dispatchers.IO) {
            val crtFrame = daoDbFrame.getCrtFrame()
            Timber.i("getCrtFrame(): State is: ${Settings.matchState}, CrtFrame is: ${crtFrame?.frame?.frameId}, frameCount is: ${Settings.crtFrame}")
            crtFrame
        }
    }

    suspend fun saveCrtFrame(frame: DomainFrame) = withContext(Dispatchers.IO) {
        // Frame
        daoDbFrame.insertOrUpdateMatchFrame(frame.asDbFrame())

        // Score
        for (dbScore in frame.asDbCrtScore()) daoDbScore.insertOrUpdateMatchScore(dbScore)

        // Breaks - Only check breaks from crt frame
        val dbBreaks = frame.asDbBreaks()
        dbBreaks.lastOrNull()?.let { daoDbBreak.insertOrUpdateMatchBreak(it) }

        for (dbBreakId in daoDbBreak.getCrtFrameBreaks(frame.frameId)
            .map { it.breakId }) { // If break exists in frameStack, but not in Db, remove from Db
            if (!dbBreaks.map { it.breakId }.contains(dbBreakId)) daoDbBreak.deleteMatchBreak(dbBreakId)
        }

        // Pots - only check pots from crt break
        frame.frameStack.lastOrNull()?.apply {
            val dbBreakPots = asDbPots(breakId)
            dbBreakPots.lastOrNull()?.let { daoDbPot.insertOrUpdateBreakPot(it) }
            for (dbPotId in daoDbPot.getCrtBreakPots(breakId)
                .map { it.potId }) { // If pot exists in break, but not in Db, remove from Db
                if (!dbBreakPots.map { it.potId }.contains(dbPotId)) daoDbPot.deleteBreakPot(dbPotId)
            }
        }

        // Ballstack - Only check balls for crt frame
        val dbBallStack = frame.asDbBallStack()
        for (dbBall in dbBallStack) daoDbBall.insertOrUpdateMatchBall(dbBall)
        for (dbBallId in daoDbBall.getMatchBalls().map { it.ballId }) { // If ball exists in ballStack, but not in Db, remove from Db
            if (!dbBallStack.map { it.ballId }.contains(dbBallId)) daoDbBall.deleteMatchBall(dbBallId)
        }

        // Debug Actions - Only check actions for crt frameF
        frame.asDbDebugFrameActions().lastOrNull()?.let { daoDbActionLog.insertOrUpdateDebugFrameActions(it) }
        Timber.i("saveCrtFrame(): id: ${frame.frameId} score: ${frame.score[0].framePoints} vs ${frame.score[1].framePoints} ")
    }

    suspend fun deleteCrtFrame(frameId: Long) = withContext(Dispatchers.IO) {
        daoDbFrame.deleteCrtFrame(frameId)
        daoDbScore.deleteCrtFrameScore(frameId)
        val crtBreaks = daoDbBreak.getCrtFrameBreaks(frameId)
        crtBreaks.forEach {
            daoDbPot.deleteCrtBreakPots(it.breakId)
        }
        daoDbBreak.deleteCrtFrameBreaks(frameId)
        daoDbBall.deleteCrtFrameBalls(frameId)
        daoDbActionLog.deleteCrtFrameActions(frameId)
        Timber.i("Delete crt frame $frameId")
    }

    suspend fun deleteCrtMatch() = withContext(Dispatchers.IO) {
        daoDbScore.clear()
        daoDbBreak.clear()
        daoDbPot.clear()
        daoDbBall.clear()
        daoDbFrame.clear()
        daoDbActionLog.clear()
        Timber.i("deleteCrtMatch()")
    }

    // Get the crt score from the database
    val score: LiveData<ArrayList<Pair<DomainScore, DomainScore>>> = daoDbScore.getMatchScore().map {
        it.asDomainPair()
    }

    suspend fun getDomainActionLogs(): List<DomainActionLog> {
        return withContext(Dispatchers.IO) {
            daoDbActionLog.getDebugFrameActions().asDomain()
        }
    }
}