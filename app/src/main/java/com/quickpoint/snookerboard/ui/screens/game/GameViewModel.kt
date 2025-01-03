package com.quickpoint.snookerboard.ui.screens.game

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickpoint.snookerboard.core.utils.JobQueue
import com.quickpoint.snookerboard.core.utils.MatchAction
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_ENDED
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_ENDING_DIALOG
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_LOG_ACTIONS_DIALOG
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_MISS_FORFEIT
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_RERACK_DIALOG
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_START_NEW
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_TO_END
import com.quickpoint.snookerboard.core.utils.MatchAction.MATCH_CANCEL
import com.quickpoint.snookerboard.core.utils.MatchAction.MATCH_CANCEL_DIALOG
import com.quickpoint.snookerboard.core.utils.MatchAction.MATCH_ENDED
import com.quickpoint.snookerboard.core.utils.MatchAction.MATCH_ENDING_DIALOG
import com.quickpoint.snookerboard.core.utils.MatchAction.MATCH_TO_END
import com.quickpoint.snookerboard.core.utils.MatchAction.NAV_TO_POST_MATCH
import com.quickpoint.snookerboard.core.utils.MatchAction.SNACK_FRAME_ENDING_DIALOG
import com.quickpoint.snookerboard.core.utils.MatchAction.SNACK_FRAME_RERACK_DIALOG
import com.quickpoint.snookerboard.core.utils.MatchAction.SNACK_MATCH_ENDING_DIALOG
import com.quickpoint.snookerboard.core.utils.MatchAction.SNACK_UNDO
import com.quickpoint.snookerboard.domain.models.BallFactory
import com.quickpoint.snookerboard.domain.models.DomainActionLogsManager
import com.quickpoint.snookerboard.domain.models.DomainBall
import com.quickpoint.snookerboard.domain.models.DomainBallManager
import com.quickpoint.snookerboard.domain.models.DomainBreakManager
import com.quickpoint.snookerboard.domain.models.DomainFrame
import com.quickpoint.snookerboard.domain.models.DomainFrameManager
import com.quickpoint.snookerboard.domain.models.DomainScoreManager
import com.quickpoint.snookerboard.domain.models.PotAction
import com.quickpoint.snookerboard.domain.models.PotAction.FIRST
import com.quickpoint.snookerboard.domain.models.PotType
import com.quickpoint.snookerboard.domain.models.isFrameAndMatchEqual
import com.quickpoint.snookerboard.domain.models.isFrameEqual
import com.quickpoint.snookerboard.domain.models.isFrameInProgress
import com.quickpoint.snookerboard.domain.models.isMatchInProgress
import com.quickpoint.snookerboard.domain.repository.DataStoreRepository
import com.quickpoint.snookerboard.domain.repository.GameRepository
import com.quickpoint.snookerboard.domain.usecases.AssignPotUseCase
import com.quickpoint.snookerboard.domain.usecases.EmailLogsUseCase
import com.quickpoint.snookerboard.domain.usecases.EndFrameUseCase
import com.quickpoint.snookerboard.domain.usecases.LoadMatchUseCase
import com.quickpoint.snookerboard.domain.usecases.ResetFrameUseCase
import com.quickpoint.snookerboard.domain.usecases.ResetMatchUseCase
import com.quickpoint.snookerboard.domain.utils.MatchConfig
import com.quickpoint.snookerboard.ui.navigation.MenuItem
import com.quickpoint.snookerboard.ui.navigation.MenuItemIds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    val dataStoreRepository: DataStoreRepository,
    val ballFactory: BallFactory,
    val ballManager: DomainBallManager,
    private val breakManager: DomainBreakManager,
    val scoreManager: DomainScoreManager,
    val matchConfig: MatchConfig,
    val frameManager: DomainFrameManager,
    private val actionLogsManager: DomainActionLogsManager,

    private val loadMatchUseCase: LoadMatchUseCase,
    private val resetMatchUseCase: ResetMatchUseCase,
    private val resetFrameUseCase: ResetFrameUseCase,
    private val endFrameUseCase: EndFrameUseCase,
    private val emailLogsUseCase: EmailLogsUseCase,
    private val assignPotUseCase: AssignPotUseCase
) : ViewModel() {

    private lateinit var jobQueue: JobQueue
    fun getDisplayFrames() = matchConfig.formatAvailableFramesForDisplay()

    init {
        observeBreakListFlow()
    }

    private val _eventAction = MutableSharedFlow<MatchAction?>()
    val eventAction = _eventAction.asSharedFlow()
    fun onEventGameAction(matchAction: MatchAction?, queue: Boolean = false): Boolean {
        viewModelScope.launch {
            if (queue) jobQueue.submit { _eventAction.emit(matchAction) }
            else _eventAction.emit(matchAction)
        }
        return matchAction != null
    }

    val frameState = frameManager.frameState

    private fun observeBreakListFlow() {
        viewModelScope.launch {
            assignPotUseCase.eventFlow.collect { (action, enqueueable) ->
                onEventGameAction(action, enqueueable)
            }
        }
        viewModelScope.launch {
            breakManager.breakListFlow.collect { breakList ->
                Timber.e("Break List: $breakList")
            }
        }
        viewModelScope.launch {
            ballManager.ballsListFlow.collect { ballList ->
                frameManager.updateFrame {
                    copy(ballsList = ballList)
                }
            }
        }
        viewModelScope.launch {
            scoreManager.scoreListFlow.collect { scoreList ->
                frameManager.updateFrame {
                    copy(scoreList = scoreList)
                }
            }
        }
        viewModelScope.launch {
            frameManager.frameState.collect { domainFrame ->
                if (actionLogsManager.actionLogs.value.isNotEmpty()) {
                    gameRepository.saveCrtFrame(domainFrame)
                }
            }
        }
    }

    fun loadMatch(frame: DomainFrame?) = frame?.let {
        jobQueue = JobQueue()
        loadMatchUseCase(frame)
    }

    fun resetMatch() {
        jobQueue = JobQueue()
        resetMatchUseCase()
    }

    fun resetFrame(matchAction: MatchAction) {
        resetFrameUseCase(matchAction)
    }

    fun endFrame(matchAction: MatchAction) {
        endFrameUseCase()
        if (matchAction in listOf(FRAME_MISS_FORFEIT, FRAME_TO_END, FRAME_ENDED)) onEventGameAction(FRAME_START_NEW, true)
        if (matchAction in listOf(MATCH_TO_END, MATCH_ENDED)) onEventGameAction(NAV_TO_POST_MATCH, true)
    }

    fun assignPot(potType: PotType?, ball: DomainBall? = null, action: PotAction = FIRST) = viewModelScope.launch {
        assignPotUseCase(potType, ball, action)
    }

    fun emailLogs(context: Context) = viewModelScope.launch {
        emailLogsUseCase(context)
    }

    fun onMenuItemSelected(menuItem: MenuItem) {
        when (menuItem.id) {
            MenuItemIds.ID_MENU_ITEM_LOG -> onEventGameAction(FRAME_LOG_ACTIONS_DIALOG)
            MenuItemIds.ID_MENU_ITEM_UNDO -> if (frameState.value.breaksList.isFrameInProgress()) assignPot(null) else onEventGameAction(
                SNACK_UNDO
            )
            MenuItemIds.ID_MENU_ITEM_RERACK -> onEventGameAction(if (frameState.value.breaksList.isFrameInProgress()) FRAME_RERACK_DIALOG else SNACK_FRAME_RERACK_DIALOG)
            MenuItemIds.ID_MENU_ITEM_CONCEDE_FRAME -> onEventGameAction(if (!frameState.value.scoreList.isFrameEqual()) FRAME_ENDING_DIALOG else SNACK_FRAME_ENDING_DIALOG)
            MenuItemIds.ID_MENU_ITEM_CONCEDE_MATCH -> onEventGameAction(if (!frameState.value.scoreList.isFrameAndMatchEqual()) MATCH_ENDING_DIALOG else SNACK_MATCH_ENDING_DIALOG)
            MenuItemIds.ID_MENU_ITEM_CANCEL_MATCH -> onEventGameAction(if (frameState.value.scoreList.isMatchInProgress()) MATCH_CANCEL_DIALOG else MATCH_CANCEL)
            else -> {}
        }
    }
}