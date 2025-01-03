package com.quickpoint.snookerboard.domain.usecases

import com.quickpoint.snookerboard.domain.models.DomainActionLog
import com.quickpoint.snookerboard.domain.models.DomainActionLogsManager
import com.quickpoint.snookerboard.domain.models.DomainBallManager
import com.quickpoint.snookerboard.domain.models.DomainBreakManager
import com.quickpoint.snookerboard.domain.models.DomainFrame
import com.quickpoint.snookerboard.domain.models.DomainScoreManager
import javax.inject.Inject

class LoadMatchUseCase @Inject constructor(
    private val ballManager: DomainBallManager,
    private val breakManager: DomainBreakManager,
    private val scoreManager: DomainScoreManager,
    private val actionLogsManager: DomainActionLogsManager,
) {
    operator fun invoke(frame: DomainFrame) {
        scoreManager.updateScoreList(frame.scoreList)
        ballManager.updateBallsList(frame.ballsList)
        breakManager.updateBreaksList(frame.breaksList)
        actionLogsManager.addNewLog(DomainActionLog("loadMatch(): ${frame.getTextInfo()}"))
    }
}