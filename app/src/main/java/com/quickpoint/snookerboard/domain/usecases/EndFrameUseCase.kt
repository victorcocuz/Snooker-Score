package com.quickpoint.snookerboard.domain.usecases

import com.quickpoint.snookerboard.domain.models.DomainActionLog
import com.quickpoint.snookerboard.domain.models.DomainActionLogsManager
import com.quickpoint.snookerboard.domain.models.DomainScoreManager
import javax.inject.Inject

class EndFrameUseCase @Inject constructor(
    private val scoreManager: DomainScoreManager,
    private val actionLogsManager: DomainActionLogsManager,
) {
    operator fun invoke() {
        scoreManager.endFrame()
        actionLogsManager.addNewLog(DomainActionLog("endFrame()"))
    }
}