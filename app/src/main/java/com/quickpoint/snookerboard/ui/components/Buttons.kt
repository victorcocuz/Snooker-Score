package com.quickpoint.snookerboard.ui.components

import android.widget.ImageButton
import android.widget.ImageView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.quickpoint.snookerboard.R
import com.quickpoint.snookerboard.core.base.Event
import com.quickpoint.snookerboard.core.utils.BallAdapterType
import com.quickpoint.snookerboard.data.K_INT_MATCH_HANDICAP_FRAME
import com.quickpoint.snookerboard.domain.models.DomainBall
import com.quickpoint.snookerboard.domain.models.DomainBall.BLACK
import com.quickpoint.snookerboard.domain.models.DomainBall.BLUE
import com.quickpoint.snookerboard.domain.models.DomainBall.BROWN
import com.quickpoint.snookerboard.domain.models.DomainBall.COLOR
import com.quickpoint.snookerboard.domain.models.DomainBall.FREEBALL
import com.quickpoint.snookerboard.domain.models.DomainBall.GREEN
import com.quickpoint.snookerboard.domain.models.DomainBall.NOBALL
import com.quickpoint.snookerboard.domain.models.DomainBall.PINK
import com.quickpoint.snookerboard.domain.models.DomainBall.RED
import com.quickpoint.snookerboard.domain.models.DomainBall.YELLOW
import com.quickpoint.snookerboard.domain.utils.MatchConfig
import com.quickpoint.snookerboard.domain.utils.getHandicap
import com.quickpoint.snookerboard.domain.utils.getSettingsTextIdByKeyAndValue
import com.quickpoint.snookerboard.domain.utils.isSettingsButtonSelected
import com.quickpoint.snookerboard.ui.screens.rules.RulesViewModel
import com.quickpoint.snookerboard.ui.theme.Beige
import com.quickpoint.snookerboard.ui.theme.Black
import com.quickpoint.snookerboard.ui.theme.CreamBright
import com.quickpoint.snookerboard.ui.theme.Green
import com.quickpoint.snookerboard.ui.theme.White
import com.quickpoint.snookerboard.ui.theme.spacing
import kotlinx.coroutines.launch


@Composable
fun ClickableText(text: String, onClick: () -> Unit) = Button(
    modifier = Modifier, onClick = { onClick() }, shape = RoundedCornerShape(MaterialTheme.spacing.extraSmall)
) {
    Text(textAlign = TextAlign.Center, text = text.uppercase(), style = MaterialTheme.typography.labelLarge)
}

@Composable
fun RulesHandicapLabel(
    rulesVm: RulesViewModel,
    key: String,
    matchConfig: MatchConfig
) {
    val rulesUpdateAction by rulesVm.eventMatchSettingsChange.collectAsState(Event(Unit))
    var handicap by remember { mutableIntStateOf(0) }
    LaunchedEffect(rulesUpdateAction) {
        handicap = when (key) {
            K_INT_MATCH_HANDICAP_FRAME -> matchConfig.handicapFrame
            else -> matchConfig.handicapMatch
        }
    }
    Text(
        modifier = Modifier.width(60.dp),
        textAlign = TextAlign.Center,
        text = "${getHandicap(handicap, -1)} - ${getHandicap(handicap, 1)}"
    )
}

@Composable
fun ButtonStandardHoist(
    rulesVm: RulesViewModel,
    key: String,
    value: Int = -2,
) {
    val rulesUpdateAction by rulesVm.eventMatchSettingsChange.collectAsState(Event(Unit))
    val matchConfig = rulesVm.matchConfig
    LaunchedEffect(key1 = rulesUpdateAction, block = {}) // Used to refresh composition when rules change
    ButtonStandard(
        text = stringResource(getSettingsTextIdByKeyAndValue(key, value)),
        isSelected = isSettingsButtonSelected(key, value, matchConfig),
        onClick = { rulesVm.onMatchSettingsChange(key, value) }
    )
}

@Composable
fun ButtonStandard(
    modifier: Modifier = Modifier,
    text: String,
    height: Dp = 40.dp,
    isSelected: Boolean = false,
    isEnabled: Boolean = true,
    onClick: () -> Unit,
) = OutlinedButton(
    modifier = modifier.height(height),
    contentPadding = PaddingValues(20.dp, 8.dp),
    shape = RoundedCornerShape(MaterialTheme.spacing.extraSmall),
    border = BorderStroke(1.dp, if (isSelected) Beige else Black),
    onClick = onClick,
    colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Green else CreamBright),
    enabled = isEnabled
) {
    Text(
        text = text.uppercase(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelLarge.copy(color = if (isSelected) White else Black)
    )
}


@Composable
fun IconButton(
    modifier: Modifier = Modifier,
    text: String,
    painter: Painter,
    isSelected: Boolean = false,
    isEnabled: Boolean = true,
    onClick: () -> Unit,
) {
    OutlinedButton(
        modifier = modifier
            .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
            .padding(2.dp)
            .size(60.dp),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(MaterialTheme.spacing.extraSmall),
        border = BorderStroke(1.dp, if (isSelected && isEnabled) Beige else Black),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected && isEnabled) Green else Beige
        ),
        enabled = isEnabled
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                tint = if (isSelected) White else Black,
                modifier = Modifier.size(32.dp),
                painter = painter, contentDescription = null
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall.copy(color = if (isSelected) White else Black)
            )
        }
    }
}

@Composable
fun BallView(
    modifier: Modifier = Modifier,
    ball: DomainBall,
    ballAdapterType: BallAdapterType,
    isBallSelected: Boolean = false,
    text: String = "",
    onClick: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    Box(contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = modifier
                .aspectRatio(1f)
                .padding(2.dp),
            factory = { context ->
                ImageButton(context).apply {
                    setOnClickListener { coroutineScope.launch { onClick() } }
                    isSelected = isBallSelected
                }
            })
        { it.setBallBackground(ball, ballAdapterType, isBallSelected) }
        TextBallInfo(text)
    }
}

fun ImageView.setBallBackground(item: DomainBall?, ballAdapterType: BallAdapterType, isBallSelected: Boolean) {
    item?.let {
        // TODO: animate ball changes
//        if (ballAdapterType == MATCH) startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in_short))
        val ripple = ballAdapterType != BallAdapterType.BREAK
        setBackgroundResource(
            when (item) {
                // COLOR is for potting extra red
                is RED, is COLOR -> if (isBallSelected) R.drawable.ic_ball_red_pressed else if (ripple) R.drawable.ic_ball_red else R.drawable.ic_ball_red_normal
                is YELLOW -> if (isBallSelected) R.drawable.ic_ball_yellow_pressed else if (ripple) R.drawable.ic_ball_yellow else R.drawable.ic_ball_yellow_normal
                is GREEN -> if (isBallSelected) R.drawable.ic_ball_green_pressed else if (ripple) R.drawable.ic_ball_green else R.drawable.ic_ball_green_normal
                is BROWN -> if (isBallSelected) R.drawable.ic_ball_brown_pressed else if (ripple) R.drawable.ic_ball_brown else R.drawable.ic_ball_brown_normal
                is BLUE -> if (isBallSelected) R.drawable.ic_ball_blue_pressed else if (ripple) R.drawable.ic_ball_blue else R.drawable.ic_ball_blue_normal
                is PINK -> if (isBallSelected) R.drawable.ic_ball_pink_pressed else if (ripple) R.drawable.ic_ball_pink else R.drawable.ic_ball_pink_normal
                is BLACK -> if (isBallSelected) R.drawable.ic_ball_black_pressed else if (ripple) R.drawable.ic_ball_black else R.drawable.ic_ball_black_normal
                is FREEBALL -> if (isBallSelected) R.drawable.ic_ball_free_pressed else if (ripple) R.drawable.ic_ball_free else R.drawable.ic_ball_free_normal
                is NOBALL -> if (ripple) R.drawable.ic_ball_miss else R.drawable.ic_ball_miss_normal
                else -> if (isBallSelected) R.drawable.ic_ball_white_pressed else if (ripple) R.drawable.ic_ball_white else R.drawable.ic_ball_white_normal
            }
        )
    }
}

@Composable
fun MainButton(text: String, onclick: () -> Unit) = Button(
    shape = RoundedCornerShape(48.dp),
    onClick = onclick,
) { TextSubtitle(text.uppercase()) }
