package com.quickpoint.snookerboard.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.quickpoint.snookerboard.MainViewModel
import com.quickpoint.snookerboard.core.ScreenEvents
import com.quickpoint.snookerboard.core.admob.showInterstitialAd
import com.quickpoint.snookerboard.core.utils.MatchAction.FOUL_CONFIRM
import com.quickpoint.snookerboard.core.utils.MatchAction.FOUL_DIALOG
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_ENDED
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_ENDING_DIALOG
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_FREE_ACTIVE
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_LAST_BLACK_FOULED
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_LAST_BLACK_FOULED_DIALOG
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_LOG_ACTIONS
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_LOG_ACTIONS_DIALOG
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_MISS_FORFEIT
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_MISS_FORFEIT_DIALOG
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_REMOVE_RED
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_RERACK
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_RERACK_DIALOG
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_RESPOT_BLACK
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_RESPOT_BLACK_DIALOG
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_START_NEW
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_TO_END
import com.quickpoint.snookerboard.core.utils.MatchAction.FRAME_UNDO
import com.quickpoint.snookerboard.core.utils.MatchAction.MATCH_CANCEL
import com.quickpoint.snookerboard.core.utils.MatchAction.MATCH_CANCEL_DIALOG
import com.quickpoint.snookerboard.core.utils.MatchAction.MATCH_ENDED
import com.quickpoint.snookerboard.core.utils.MatchAction.MATCH_ENDED_DISCARD_FRAME
import com.quickpoint.snookerboard.core.utils.MatchAction.MATCH_ENDING_DIALOG
import com.quickpoint.snookerboard.core.utils.MatchAction.MATCH_TO_END
import com.quickpoint.snookerboard.core.utils.MatchAction.NAV_TO_POST_MATCH
import com.quickpoint.snookerboard.core.utils.MatchAction.SNACK_ADD_RED
import com.quickpoint.snookerboard.core.utils.MatchAction.SNACK_FRAME_ENDING_DIALOG
import com.quickpoint.snookerboard.core.utils.MatchAction.SNACK_FRAME_RERACK_DIALOG
import com.quickpoint.snookerboard.core.utils.MatchAction.SNACK_INVALID_FOUL
import com.quickpoint.snookerboard.core.utils.MatchAction.SNACK_MATCH_ENDING_DIALOG
import com.quickpoint.snookerboard.core.utils.MatchAction.SNACK_NO_BALL
import com.quickpoint.snookerboard.core.utils.MatchAction.SNACK_REMOVE_COLOR
import com.quickpoint.snookerboard.core.utils.MatchAction.SNACK_UNDO
import com.quickpoint.snookerboard.core.utils.getListOfDialogActions
import com.quickpoint.snookerboard.core.utils.getPotType
import com.quickpoint.snookerboard.core.utils.queryEndFrameOrMatch
import com.quickpoint.snookerboard.domain.models.BallType
import com.quickpoint.snookerboard.domain.models.PotType
import com.quickpoint.snookerboard.domain.models.isMatchInProgress
import com.quickpoint.snookerboard.domain.models.isNoFrameFinished
import com.quickpoint.snookerboard.domain.utils.MatchState
import com.quickpoint.snookerboard.ui.components.BackPressHandler
import com.quickpoint.snookerboard.ui.components.ComponentPlayerNames
import com.quickpoint.snookerboard.ui.components.FragmentContent
import com.quickpoint.snookerboard.ui.components.FragmentExtras
import com.quickpoint.snookerboard.ui.navigation.Screen
import com.quickpoint.snookerboard.ui.navigation.getActionItems
import com.quickpoint.snookerboard.ui.navigation.getActionItemsOverflow
import com.quickpoint.snookerboard.ui.screens.gamedialogs.DialogFoul
import com.quickpoint.snookerboard.ui.screens.gamedialogs.DialogGeneric
import com.quickpoint.snookerboard.ui.screens.gamedialogs.DialogViewModel
import com.quickpoint.snookerboard.ui.screens.rules.RulesViewModel
import com.quickpoint.snookerboard.ui.theme.Transparent
import com.quickpoint.snookerboard.ui.theme.spacing
import timber.log.Timber

@Composable
fun ScreenGame() {
    val mainVm = LocalView.current.findViewTreeViewModelStoreOwner().let { hiltViewModel<MainViewModel>(it!!) }
    val gameVm = hiltViewModel<GameViewModel>()
    val rulesVm = hiltViewModel<RulesViewModel>()
    val dialogVm = hiltViewModel<DialogViewModel>()

    val context = LocalContext.current
    val frameState by gameVm.frameState.collectAsState(initial = null)

    val ballManager = gameVm.ballManager
    val frameManager = gameVm.frameManager
    val ballFactory = gameVm.ballFactory
    val matchConfig = gameVm.matchConfig
    val isFreeballActive by gameVm.dataStoreRepository.toggleFreeball.collectAsState(false)
    val isAdvancedBreaksActive by gameVm.dataStoreRepository.toggleAdvancedBreaks.collectAsState(false)
    val players by rulesVm.players.collectAsState()

    mainVm.setupActionBarActions(
        gameVm.getActionItems(),
        gameVm.getActionItemsOverflow()
    ) { gameVm.onMenuItemSelected(it) }

    LaunchedEffect(Unit) {
        if (matchConfig.matchState == MatchState.RULES_IDLE) {
            gameVm.resetMatch()
            matchConfig.matchState = MatchState.GAME_IN_PROGRESS
        } else {
            gameVm.loadMatch(mainVm.cachedFrame)
        }

        gameVm.eventAction.collect { action ->
            when (action) {
                // Directly observed from gameVm
                SNACK_UNDO, SNACK_ADD_RED, SNACK_REMOVE_COLOR, SNACK_FRAME_RERACK_DIALOG,
                SNACK_FRAME_ENDING_DIALOG, SNACK_MATCH_ENDING_DIALOG, SNACK_NO_BALL,
                -> mainVm.onEmit(ScreenEvents.SnackAction(action))

                // Concerning dialogs
                FOUL_DIALOG -> dialogVm.onOpenFoulDialog()
                FOUL_CONFIRM -> {
                    gameVm.assignPot(PotType.TYPE_FOUL, dialogVm.ballClicked!!, dialogVm.actionClicked.value) // Temp Pot Action
                    dialogVm.onDismissFoulDialog()
                }

                FRAME_LOG_ACTIONS_DIALOG, FRAME_LAST_BLACK_FOULED_DIALOG, FRAME_RESPOT_BLACK_DIALOG, FRAME_RERACK_DIALOG, FRAME_ENDING_DIALOG, MATCH_ENDING_DIALOG,
                MATCH_CANCEL_DIALOG, FRAME_MISS_FORFEIT_DIALOG,
                -> {
                    val actions =
                        action.getListOfDialogActions(
                            gameVm.scoreManager.isMatchEnding(),
                            gameVm.frameState.value.scoreList.isNoFrameFinished(),
                            gameVm.frameManager.isFrameMathematicallyOver()
                        )
                    dialogVm.onOpenGenericDialog(actions)
                }

                FRAME_LOG_ACTIONS -> gameVm.emailLogs(context)
                FRAME_FREE_ACTIVE, FRAME_UNDO, FRAME_REMOVE_RED, FRAME_LAST_BLACK_FOULED, FRAME_RESPOT_BLACK -> gameVm.assignPot(action.getPotType())
                FRAME_MISS_FORFEIT -> gameVm.onEventGameAction(
                    action.queryEndFrameOrMatch(
                        gameVm.scoreManager.isMatchEnding(),
                        gameVm.frameManager.isFrameMathematicallyOver()
                    )
                )

                FRAME_TO_END, FRAME_ENDED, MATCH_TO_END, MATCH_ENDED -> gameVm.endFrame(action)
                FRAME_RERACK, FRAME_START_NEW -> {
                    showInterstitialAd(context)
                    gameVm.resetFrame(action)
                }

                MATCH_ENDED_DISCARD_FRAME -> {
                    mainVm.deleteCrtFrameFromDb()
                    gameVm.onEventGameAction(NAV_TO_POST_MATCH)
                }

                NAV_TO_POST_MATCH -> mainVm.onEmit(ScreenEvents.Navigate(Screen.Summary.route))
                MATCH_CANCEL -> {
                    mainVm.deleteMatchFromDb()
                    mainVm.onEmit(ScreenEvents.Navigate(Screen.Rules.route))
                }

                else -> Timber.i("No implementation for observed action $action")
            }
        }
    }

    val crtPlayer by remember { mutableIntStateOf(gameVm.matchConfig.crtPlayer) }

    frameState?.let { domainFrame ->
        FragmentContent(
            Modifier.background(Transparent),
            paddingValues = PaddingValues(MaterialTheme.spacing.default),
            showBottomSpacer = false
        ) {
            ComponentPlayerNames(crtPlayer, players)
            ModuleGameScore(domainFrame, frameManager, gameVm.getDisplayFrames())
            ModuleGameStatistics(domainFrame.scoreList, domainFrame.scoreList.size == 2)
            ModuleGameBreaks(domainFrame.breaksList, isAdvancedBreaksActive)
            ModuleGameActions(gameVm, ballFactory.createBall(BallType.TYPE_NOBALL), ballManager, domainFrame.ballsList, domainFrame.breaksList)
        }

        FragmentExtras {
            DialogGeneric(dialogVm, gameVm,
                onDismiss = { dialogVm.onDismissGenericDialog() },
                onConfirm = { matchAction -> dialogVm.onEventDialogAction(matchAction) })
            DialogFoul(gameVm, dialogVm, ballManager, frameManager, matchConfig,
                onDismiss = { dialogVm.onDismissFoulDialog() },
                onConfirm = {
                    if (dialogVm.foulIsValid()) {
                        repeat(dialogVm.eventDialogReds.value) { gameVm.onEventGameAction(FRAME_REMOVE_RED, true) }
                        gameVm.onEventGameAction(FOUL_CONFIRM, true)
                        if (isFreeballActive) gameVm.onEventGameAction(FRAME_FREE_ACTIVE, true)
                    } else mainVm.onEmit(ScreenEvents.SnackAction(SNACK_INVALID_FOUL))
                })
            BackPressHandler {
                gameVm.onEventGameAction(
                    if (domainFrame.scoreList.isMatchInProgress()) MATCH_CANCEL_DIALOG else MATCH_CANCEL
                )
            }
        }
    }

}




