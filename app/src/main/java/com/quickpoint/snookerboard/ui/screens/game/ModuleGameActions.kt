package com.quickpoint.snookerboard.ui.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quickpoint.snookerboard.R
import com.quickpoint.snookerboard.core.utils.BallAdapterType
import com.quickpoint.snookerboard.data.K_BOOL_TOGGLE_LONG_SHOT
import com.quickpoint.snookerboard.data.K_BOOL_TOGGLE_REST_SHOT
import com.quickpoint.snookerboard.domain.models.DomainBall
import com.quickpoint.snookerboard.domain.models.DomainBallManager
import com.quickpoint.snookerboard.domain.models.DomainBreak
import com.quickpoint.snookerboard.domain.models.PotType
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_ADDRED
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_FOUL_ATTEMPT
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_MISS
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_REMOVE_COLOR
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_SAFE
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_SAFE_MISS
import com.quickpoint.snookerboard.domain.models.PotType.TYPE_SNOOKER
import com.quickpoint.snookerboard.ui.components.BallView
import com.quickpoint.snookerboard.ui.components.ContainerColumn
import com.quickpoint.snookerboard.ui.components.GameButtonsBalls
import com.quickpoint.snookerboard.ui.components.IconButton
import com.quickpoint.snookerboard.ui.components.StandardRow
import com.quickpoint.snookerboard.ui.theme.BrownDark

@Composable
fun ModuleGameActions(
    gameVm: GameViewModel,
    ball: DomainBall,
    ballManager: DomainBallManager,
    ballsList: List<DomainBall>,
    breaksList: List<DomainBreak>,
) =
    ContainerColumn(Modifier.background(BrownDark)) {
        val ballSize = 48.dp
        GameButtonsIcons(gameVm)
        StandardRow {
            GameButtonsBalls(ballsList, ballManager, ballSize) { potType, domainBall -> gameVm.assignPot(potType, domainBall) }
            GameButtonsBallsExtra(
                ballsList,
                ball,
                ballSize,
                ballManager.canRemoveColor(breaksList),
                ballManager.isAddRedAvailable()
            ) { potType -> gameVm.assignPot(potType, ball) }
        }
    }

@Composable
fun GameButtonsIcons(gameVm: GameViewModel) {
    val isLongActive by gameVm.dataStoreRepository.toggleLongShot.collectAsState(false)
    val isRestActive by gameVm.dataStoreRepository.toggleRestShot.collectAsState(false)
    val isAdvancedStatistics by gameVm.dataStoreRepository.toggleAdvancedStatistics.collectAsState(false)

    StandardRow(
        Modifier
            .fillMaxWidth()
            .padding(0.dp, 8.dp)
    ) {
        IconButton(
            text = stringResource(R.string.l_game_actions_btn_foul),
            painter = painterResource(R.drawable.ic_action_foul)
        ) { gameVm.assignPot(TYPE_FOUL_ATTEMPT) }
        IconButton(
            text = stringResource(R.string.l_game_actions_btn_safe_success),
            painter = painterResource(R.drawable.ic_action_safe_success),
        ) { gameVm.assignPot(TYPE_SAFE) }
        IconButton(
            text = stringResource(R.string.l_game_actions_btn_safe_miss),
            painter = painterResource(R.drawable.ic_action_safe_miss),
        ) { gameVm.assignPot(TYPE_SAFE_MISS) }
        IconButton(
            text = stringResource(R.string.l_game_actions_btn_snooker),
            painter = painterResource(R.drawable.ic_action_snooker),
        ) { gameVm.assignPot(TYPE_SNOOKER) }

        if (isAdvancedStatistics) IconButton(
            text = stringResource(R.string.l_game_actions_btn_long),
            painter = painterResource(R.drawable.ic_action_shot_type_long),
            isSelected = isLongActive
        ) { gameVm.dataStoreRepository.savePrefs(K_BOOL_TOGGLE_LONG_SHOT, !isLongActive) }
        if (isAdvancedStatistics) IconButton(
            text = stringResource(R.string.l_game_actions_btn_rest),
            painter = painterResource(R.drawable.ic_action_shot_type_rest),
            isSelected = isRestActive
        ) { gameVm.dataStoreRepository.savePrefs(K_BOOL_TOGGLE_REST_SHOT, !isRestActive) }
    }
}

@Composable
fun GameButtonsBallsExtra(
    ballsList: List<DomainBall>,
    ball: DomainBall,
    ballSize: Dp,
    isRemoveColorAvailable: Boolean = false,
    isAddRedAvailable: Boolean,
    onClick: (PotType) -> Unit = { _: PotType -> },
) = StandardRow {
    BallView(
        modifier = Modifier.size(ballSize),
        ball,
        BallAdapterType.MATCH
    ) { onClick(TYPE_MISS) }

    if (isAddRedAvailable || isRemoveColorAvailable) {
        BallView(
            modifier = Modifier.size(ballSize),
            ball = ballsList.last(),
            ballAdapterType = BallAdapterType.MATCH,
            text = stringResource(if (isAddRedAvailable) R.string.ball_add_one else R.string.ball_remove_one)
        ) { onClick(if (isAddRedAvailable) TYPE_ADDRED else TYPE_REMOVE_COLOR) }
    }
}
