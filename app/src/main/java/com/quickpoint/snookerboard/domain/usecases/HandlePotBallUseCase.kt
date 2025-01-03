package com.quickpoint.snookerboard.domain.usecases

import com.quickpoint.snookerboard.domain.models.DomainActionLogsManager
import com.quickpoint.snookerboard.domain.models.DomainBallManager
import com.quickpoint.snookerboard.domain.models.DomainBreakManager
import com.quickpoint.snookerboard.domain.models.DomainPot
import com.quickpoint.snookerboard.domain.models.DomainScoreManager
import com.quickpoint.snookerboard.domain.models.PotDirection
import com.quickpoint.snookerboard.domain.models.foulValue
import com.quickpoint.snookerboard.domain.models.getActionLog
import com.quickpoint.snookerboard.domain.utils.MatchConfig
import javax.inject.Inject

class HandlePotBallUseCase @Inject constructor(
    val matchConfig: MatchConfig,
    private val ballManager: DomainBallManager,
    private val breakManager: DomainBreakManager,
    private val scoreManager: DomainScoreManager,
    private val actionLogsManager: DomainActionLogsManager,
) {
    operator fun invoke(pot: DomainPot) {
        matchConfig.handlePotFreeballToggle(pot.potType)
        ballManager.onPot(pot.potType, pot.potAction)
        breakManager.onPot(pot)
        val highestBreak = breakManager.findMaxBreak()
        scoreManager.calculatePoints(pot, PotDirection.POT, ballManager.ballsListFlow.value.foulValue(), highestBreak)
        actionLogsManager.addNewLog(
            pot.getActionLog(
                "handlePot()",
                matchConfig.crtPlayer,
                breakManager.breakListFlow.value.size,
                ballManager.ballsListFlow.value.lastOrNull()?.ballType,
                matchConfig.crtFrame
            )
        )
        matchConfig.crtPlayer = matchConfig.getCrtPlayerFromPotAction(pot.potAction)
        matchConfig.isLongShotToggleEnabled = false
        matchConfig.isRestShotToggleEnabled = false
    }
}