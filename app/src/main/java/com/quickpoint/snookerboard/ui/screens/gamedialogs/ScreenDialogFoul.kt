package com.quickpoint.snookerboard.ui.screens.gamedialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider
import com.quickpoint.snookerboard.R
import com.quickpoint.snookerboard.core.utils.BallAdapterType
import com.quickpoint.snookerboard.core.utils.MatchAction.FOUL_DIALOG
import com.quickpoint.snookerboard.domain.models.DomainBall
import com.quickpoint.snookerboard.domain.models.DomainBallManager
import com.quickpoint.snookerboard.domain.models.DomainFrame
import com.quickpoint.snookerboard.domain.models.DomainFrameManager
import com.quickpoint.snookerboard.domain.models.PotAction
import com.quickpoint.snookerboard.domain.models.maxRemoveReds
import com.quickpoint.snookerboard.domain.utils.MatchConfig
import com.quickpoint.snookerboard.ui.components.ButtonStandard
import com.quickpoint.snookerboard.ui.components.ContainerColumn
import com.quickpoint.snookerboard.ui.components.ContainerRow
import com.quickpoint.snookerboard.ui.components.GameButtonsBalls
import com.quickpoint.snookerboard.ui.components.GenericDialog
import com.quickpoint.snookerboard.ui.components.IconButton
import com.quickpoint.snookerboard.ui.components.StandardRow
import com.quickpoint.snookerboard.ui.components.TextSubtitle
import com.quickpoint.snookerboard.ui.screens.game.GameViewModel
import com.quickpoint.snookerboard.utils.getGenericDialogTitleText

@Composable
fun DialogFoul(
    gameVm: GameViewModel,
    dialogVm: DialogViewModel,
    ballManager: DomainBallManager,
    frameManager: DomainFrameManager,
    matchConfig: MatchConfig,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (dialogVm.isFoulDialogShown) {
        val frameState by gameVm.frameState.collectAsState()
        val actionClicked by dialogVm.actionClicked.collectAsState()
        val dialogReds by dialogVm.eventDialogReds.collectAsState()
        val isCancelable = true

        GenericDialog(isCancelable = isCancelable, onDismissRequest = onDismiss) {
            TextSubtitle(getGenericDialogTitleText(FOUL_DIALOG, FOUL_DIALOG))
            FoulDialogActions(dialogVm, ballManager, frameState.ballsList)
            FoulDialogPotAction(dialogVm, frameManager, actionClicked)
            FoulDialogRedsPottedSlider(
                dialogReds,
                frameState.ballsList.maxRemoveReds().toFloat(),
                frameState.ballsList.maxRemoveReds() > 0
            ) {
                dialogVm.onDialogReds(it)
            }
            FoulDialogOtherActions(gameVm, matchConfig, frameState, actionClicked)
            ContainerRow {
                ButtonStandard(text = stringResource(R.string.f_dialog_foul_btn_cancel)) { onDismiss() }
                ButtonStandard(text = stringResource(R.string.f_dialog_foul_btn_submit)) { onConfirm() }
            }
        }
    }
}

@Composable
fun FoulDialogActions(dialogVm: DialogViewModel, ballManager: DomainBallManager, balls: List<DomainBall>) = BoxWithConstraints {
    var selectionPosition by remember { mutableLongStateOf(-1L) }
    val ballSize = maxWidth / 8

    ContainerRow(title = stringResource(R.string.f_dialog_foul_tv_balls_label)) {
        GameButtonsBalls(balls, ballManager, ballSize, BallAdapterType.FOUL, selectionPosition) { _, domainBall ->
            selectionPosition = domainBall.ballId
            dialogVm.onBallClicked(domainBall)
        }
    }
}

@Composable
fun FoulDialogPotAction(
    dialogVm: DialogViewModel,
    frameManager: DomainFrameManager,
    actionClicked: PotAction,
) = ContainerRow(title = stringResource(R.string.f_dialog_foul_tv_action_label)) {
    ButtonStandard(
        Modifier.weight(1f),
        text = stringResource(R.string.f_dialog_foul_btn_continue),
        height = 56.dp,
        isSelected = actionClicked == PotAction.SWITCH
    ) { dialogVm.onActionClicked(PotAction.SWITCH) }
    Spacer(Modifier.width(8.dp))
    ButtonStandard(
        Modifier.weight(1f),
        text = stringResource(R.string.f_dialog_foul_btn_force_continue),
        height = 56.dp,
        isSelected = actionClicked == PotAction.CONTINUE,
        isEnabled = frameManager.isFoulAndAMiss()
    ) { dialogVm.onActionClicked(PotAction.CONTINUE) }
    Spacer(Modifier.width(8.dp))
    ButtonStandard(
        Modifier.weight(1f),
        text = stringResource(R.string.f_dialog_foul_btn_force_retake),
        height = 56.dp,
        isSelected = actionClicked == PotAction.RETAKE,
        isEnabled = frameManager.isFoulAndAMiss()
    ) { dialogVm.onActionClicked(PotAction.RETAKE) }
}

@Composable
fun FoulDialogRedsPottedSlider(
    dialogReds: Int,
    rangeTop: Float,
    show: Boolean = false,
    onValueChange: (Int) -> Unit,
) {
    if (show) ContainerColumn(title = stringResource(R.string.f_dialog_foul_tv_reds_label)) {
        AndroidView(modifier = Modifier.fillMaxWidth(), factory = { context ->
            Slider(context).apply {
                stepSize = 1f
                value = 0f
                valueFrom = 0f
                valueTo = rangeTop
                labelBehavior = LabelFormatter.LABEL_GONE
                addOnChangeListener { _, value, _ -> onValueChange(value.toInt()) }
            }
        }, update = { slider -> slider.value = dialogReds.toFloat() })

        StandardRow(
            Modifier
                .fillMaxWidth()
                .padding(12.dp, 0.dp), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextSubtitle(stringResource(R.string._0))
            TextSubtitle(stringResource(R.string._1))
            if (rangeTop > 1) TextSubtitle(stringResource(R.string._2))
            if (rangeTop > 2) TextSubtitle(stringResource(R.string._3))
        }
    }
}

@Composable
fun FoulDialogOtherActions(
    gameVm: GameViewModel,
    matchConfig: MatchConfig,
    domainFrame: DomainFrame,
    actionClicked: PotAction,
) = ContainerRow(
    Modifier.fillMaxWidth(),
    title = stringResource(R.string.f_dialog_foul_tv_shot_type_label),
) {

    val isLongActive by gameVm.dataStoreRepository.toggleLongShot.collectAsState(false)
    val isRestActive by gameVm.dataStoreRepository.toggleRestShot.collectAsState(false)
    val isFreeballActive by gameVm.dataStoreRepository.toggleFreeball.collectAsState(false)
    val isAdvancesStatisticsActive by gameVm.dataStoreRepository.toggleAdvancedStatistics.collectAsState(false)

    val isFreeBallEnabled = actionClicked == PotAction.SWITCH && domainFrame.ballsList.size > 2
    if (!isFreeBallEnabled) matchConfig.isFreeballEnabled = false

    IconButton(
        text = stringResource(R.string.l_game_actions_btn_free_ball),
        painter = painterResource(R.drawable.ic_action_shot_type_free),
        isSelected = isFreeballActive,
        isEnabled = isFreeBallEnabled
    ) { matchConfig.isFreeballEnabled = !isFreeballActive }
    if (isAdvancesStatisticsActive) IconButton(
        text = stringResource(R.string.l_game_actions_btn_long),
        painter = painterResource(R.drawable.ic_action_shot_type_long),
        isSelected = isLongActive
    ) { matchConfig.isLongShotToggleEnabled = !isLongActive }
    if (isAdvancesStatisticsActive) IconButton(
        text = stringResource(R.string.l_game_actions_btn_rest),
        painter = painterResource(R.drawable.ic_action_shot_type_rest),
        isSelected = isRestActive
    ) { matchConfig.isRestShotToggleEnabled =!isRestActive }
}