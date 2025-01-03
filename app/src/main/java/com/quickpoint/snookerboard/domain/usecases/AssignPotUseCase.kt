package com.quickpoint.snookerboard.domain.usecases

import com.quickpoint.snookerboard.core.utils.MatchAction
import com.quickpoint.snookerboard.domain.models.DomainBall
import com.quickpoint.snookerboard.domain.models.DomainBallManager
import com.quickpoint.snookerboard.domain.models.DomainBreakManager
import com.quickpoint.snookerboard.domain.models.DomainFrameManager
import com.quickpoint.snookerboard.domain.models.DomainPot
import com.quickpoint.snookerboard.domain.models.PotAction
import com.quickpoint.snookerboard.domain.models.PotFactory
import com.quickpoint.snookerboard.domain.models.PotType
import com.quickpoint.snookerboard.domain.models.isFrameEqual
import com.quickpoint.snookerboard.domain.models.lastPotType
import com.quickpoint.snookerboard.domain.models.listOfPotTypesEnqueueableOnUndo
import com.quickpoint.snookerboard.domain.utils.MatchConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssignPotUseCase @Inject constructor(
    private val matchConfig: MatchConfig,
    private val frameManager: DomainFrameManager,
    private val handlePotBallUseCase: HandlePotBallUseCase,
    private val handleUndoPotBallUseCase: HandleUndoPotBallUseCase,
    private val breakManager: DomainBreakManager,
    private val ballManager: DomainBallManager,
    private val potFactory: PotFactory,

    ) {

    private val _eventFlow = MutableSharedFlow<Pair<MatchAction, Boolean>>()
    val eventFlow: SharedFlow<Pair<MatchAction?, Boolean>> = _eventFlow.asSharedFlow()

    private suspend fun onEventGameAction(action: MatchAction?, enqueueable: Boolean = false): Boolean {
        action?.let {
            withContext(Dispatchers.IO) {
                _eventFlow.emit(Pair(action, enqueueable))
            }
        }
        return action != null
    }

    private var isUpdateInProgress = false // Deactivate all buttons & options menu if frame is updating

    suspend operator fun invoke(potType: PotType?, ball: DomainBall? = null, action: PotAction = PotAction.FIRST) {
        if (isUpdateInProgress) return

        isUpdateInProgress = true
        if (potType == null) {
            handleUndo()
        } else {
            val pot = potFactory.createPotWithFreeBallHandling(potType, ball, action)
            handlePot(pot)
        }
        isUpdateInProgress = false
    }

    private suspend fun handlePot(pot: DomainPot) {
        if (!handlePotExceptionsBefore(pot)) {
            handlePotBallUseCase(pot)
            handlePotExceptionsPost(pot)
        }

    }

    private suspend fun handlePotExceptionsBefore(pot: DomainPot): Boolean {
        return onEventGameAction(
            when {
                isActionTriggeringNoBallSnackMessage(pot.potType) -> MatchAction.SNACK_NO_BALL
                isPotAFoulAttempt(pot) -> MatchAction.FOUL_DIALOG
                else -> null
            }
        )
    }

    private fun isActionTriggeringNoBallSnackMessage(potType: PotType) = ballManager.isActionTriggeringNoBallSnackMessage(potType)
    private fun isPotAFoulAttempt(pot: DomainPot) = pot is DomainPot.FOULATTEMPT
    private suspend fun handlePotExceptionsPost(pot: DomainPot) {
        if (matchConfig.isVisibleBallFouledThreeTimes()) onEventGameAction(MatchAction.FRAME_MISS_FORFEIT_DIALOG, true)
        if (frameManager.isLastBlackFouledOnEqualScore(pot.potType)) onEventGameAction(MatchAction.FRAME_LAST_BLACK_FOULED_DIALOG, true)
        if (ballManager.isLastBall()) onEventGameAction(if (frameManager.frameState.value.scoreList.isFrameEqual()) MatchAction.FRAME_RESPOT_BLACK_DIALOG else MatchAction.FRAME_ENDING_DIALOG)
    }

    private suspend fun handleUndo() {
        val pot = handleUndoPotBallUseCase()
        handleUndoExceptionsPost(pot)
    }

    private suspend fun handleUndoExceptionsPost(pot: DomainPot) = onEventGameAction(
        when (pot.potType) {
            PotType.TYPE_LAST_BLACK_FOULED -> MatchAction.FRAME_UNDO
            PotType.TYPE_FREE_ACTIVE -> MatchAction.FRAME_UNDO
            PotType.TYPE_FOUL, PotType.TYPE_REMOVE_RED -> if (breakManager.breakListFlow.value.lastPotType() == PotType.TYPE_REMOVE_RED) MatchAction.FRAME_UNDO else null
            else -> null
        }, pot.potType in listOfPotTypesEnqueueableOnUndo
    )

}