package com.quickpoint.snookerboard.domain.repository

import com.quickpoint.snookerboard.domain.models.DomainActionLog
import com.quickpoint.snookerboard.domain.models.DomainFrame
import com.quickpoint.snookerboard.domain.models.DomainScore
import kotlinx.coroutines.flow.Flow

interface GameRepository {

    suspend fun getCrtFrame(): DomainFrame?
    suspend fun saveCrtFrame(frame: DomainFrame)
    suspend fun deleteCrtFrame()
    suspend fun deleteCrtMatch()
    suspend fun getTotals(playerId: Int): DomainScore
    val score: Flow<ArrayList<Pair<DomainScore, DomainScore>>>
    suspend fun getDomainActionLogs(): List<DomainActionLog>

}