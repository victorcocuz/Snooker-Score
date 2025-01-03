package com.quickpoint.snookerboard.domain.usecases

import com.quickpoint.snookerboard.domain.models.DomainActionLogsManager
import com.quickpoint.snookerboard.domain.models.DomainBallManager
import com.quickpoint.snookerboard.domain.models.DomainBreakManager
import com.quickpoint.snookerboard.domain.models.DomainFrameManager
import com.quickpoint.snookerboard.domain.models.DomainPot
import com.quickpoint.snookerboard.domain.models.DomainScoreManager
import com.quickpoint.snookerboard.domain.models.PotDirection
import com.quickpoint.snookerboard.domain.models.foulValue
import com.quickpoint.snookerboard.domain.models.getActionLog
import com.quickpoint.snookerboard.domain.models.lastPotType
import com.quickpoint.snookerboard.domain.utils.MatchConfig
import javax.inject.Inject

class HandleUndoPotBallUseCase @Inject constructor(
    val matchConfig: MatchConfig,
    private val ballManager: DomainBallManager,
    private val breakManager: DomainBreakManager,
    private val scoreManager: DomainScoreManager,
    private val actionLogsManager: DomainActionLogsManager,
    frameManager: DomainFrameManager,
) {

    private val frameState = frameManager.frameState
    operator fun invoke(): DomainPot {
        matchConfig.crtPlayer = frameState.value.breaksList.last().player
        val pot = breakManager.removeLastPotFromBreaksList()
        actionLogsManager.addNewLog(
            pot.getActionLog(
                "HandleUndo()",
                matchConfig.crtPlayer,
                frameState.value.breaksList.size,
                frameState.value.ballsList.lastOrNull()?.ballType,
                matchConfig.crtFrame
            )
        )
        ballManager.onUndo(pot.potType, pot.potAction, breakManager.breakListFlow.value)
        matchConfig.handleUndoFreeballToggle(pot.potType, breakManager.breakListFlow.value.lastPotType())
        val highestBreak = breakManager.findMaxBreak()
        scoreManager.calculatePoints(pot, PotDirection.UNDO, ballManager.ballsListFlow.value.foulValue(), highestBreak)
        matchConfig.isLongShotToggleEnabled = false
        matchConfig.isRestShotToggleEnabled = false
        return pot
    }
}