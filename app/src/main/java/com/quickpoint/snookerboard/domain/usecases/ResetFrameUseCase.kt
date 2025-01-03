package com.quickpoint.snookerboard.domain.usecases

import com.quickpoint.snookerboard.core.utils.MatchAction
import com.quickpoint.snookerboard.domain.models.DomainActionLog
import com.quickpoint.snookerboard.domain.models.DomainActionLogsManager
import com.quickpoint.snookerboard.domain.models.DomainBallManager
import com.quickpoint.snookerboard.domain.models.DomainBreakManager
import com.quickpoint.snookerboard.domain.models.DomainScoreManager
import com.quickpoint.snookerboard.domain.utils.MatchConfig
import javax.inject.Inject


class ResetFrameUseCase @Inject constructor(
    val matchConfig: MatchConfig,
    private val ballManager: DomainBallManager,
    private val breakManager: DomainBreakManager,
    private val scoreManager: DomainScoreManager,
    private val actionLogsManager: DomainActionLogsManager,
) {
    operator fun invoke(matchAction: MatchAction) {
        matchConfig.resetFrameAndGetFirstPlayer(matchAction)
        scoreManager.resetFrame(matchAction)
        ballManager.resetBalls()
        breakManager.updateBreaksList(emptyList())
        actionLogsManager.addNewLog(DomainActionLog("resetFrame()"))
    }
}