package com.quickpoint.snookerboard.ui.screens.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quickpoint.snookerboard.domain.models.DomainFrame
import com.quickpoint.snookerboard.domain.models.DomainFrameManager
import com.quickpoint.snookerboard.domain.models.frameScoreDiff
import com.quickpoint.snookerboard.ui.components.ContainerColumn
import com.quickpoint.snookerboard.ui.components.StandardRow
import com.quickpoint.snookerboard.ui.components.TextSubtitle
import com.quickpoint.snookerboard.ui.components.TextTitle
import com.quickpoint.snookerboard.ui.theme.Beige
import com.quickpoint.snookerboard.ui.theme.BrownMedium
import com.quickpoint.snookerboard.ui.theme.spacing

@Composable
fun ModuleGameScore(domainFrame: DomainFrame, frameManager: DomainFrameManager, availableFrames: String) =
    ContainerColumn(Modifier.padding(0.dp, 0.dp, 0.dp, 8.dp)) {
        val score = domainFrame.scoreList
        if (score.size == 2) {
            StandardRow(modifier = Modifier.fillMaxWidth()) {
                ScoreFrameContainer("${score[0].framePoints}")
                ScoreMatchContainer(text = "${score[0].matchPoints} $availableFrames ${score[1].matchPoints}")
                ScoreFrameContainer("${score[1].framePoints}")
            }
            ScoreProgressBar(
                description = "Remaining",
                progress = frameManager.availablePoints().toFloat() / domainFrame.frameMax,
                value = "${frameManager.availablePoints()}"
            )
            ScoreProgressBar(
                description = "Difference",
                progress = domainFrame.scoreList.frameScoreDiff().toFloat() / domainFrame.frameMax,
                value = "${domainFrame.scoreList.frameScoreDiff()}"
            )
        }
    }

@Composable
fun ScoreFrameContainer(text: String) {
    TextTitle(text, color = Beige)
}

@Composable
fun ScoreMatchContainer(text: String) {
    TextSubtitle(text, color = Beige)
}

@Composable
fun ScoreProgressBar(
    description: String,
    progress: Float,
    value: String,
) = StandardRow(
    Modifier
        .fillMaxWidth()
        .padding(0.dp, 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween
) {
    TextSubtitle(
        modifier = Modifier.width(100.dp),
        text = description,
        color = Beige
    )
    Spacer(Modifier.width(4.dp))
    LinearProgressIndicator(
        modifier = Modifier
            .height(14.dp)
            .weight(1f)
            .clip(RoundedCornerShape(MaterialTheme.spacing.extraSmall)),
        color = Beige,
        trackColor = BrownMedium,
        progress = progress
    )
    Spacer(Modifier.width(4.dp))
    TextSubtitle(
        modifier = Modifier.width(40.dp),
        text = value,
        textAlign = TextAlign.Center,
        color = Beige
    )
}