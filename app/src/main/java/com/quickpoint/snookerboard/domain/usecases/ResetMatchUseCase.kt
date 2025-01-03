package com.quickpoint.snookerboard.domain.usecases

import com.quickpoint.snookerboard.core.utils.MatchAction
import com.quickpoint.snookerboard.domain.models.DomainActionLog
import com.quickpoint.snookerboard.domain.models.DomainActionLogsManager
import com.quickpoint.snookerboard.domain.models.DomainScoreManager
import javax.inject.Inject

class ResetMatchUseCase @Inject constructor(
    private val scoreManager: DomainScoreManager,
    private val actionLogsManager: DomainActionLogsManager,
    private val resetFrameUseCase: ResetFrameUseCase
) {
    operator fun invoke(){
        scoreManager.resetMatch()
        resetFrameUseCase(MatchAction.MATCH_START_NEW)
        actionLogsManager.addNewLog(DomainActionLog("resetMatch()"))
    }
}