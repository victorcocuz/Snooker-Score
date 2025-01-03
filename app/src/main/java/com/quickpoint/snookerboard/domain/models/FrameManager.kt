package com.quickpoint.snookerboard.domain.models

import com.quickpoint.snookerboard.domain.utils.MatchConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class DomainFrameManager @Inject constructor(
    private val matchConfig: MatchConfig
) {
    private val _frameState = MutableStateFlow(DomainFrame(0, emptyList(), emptyList(), emptyList(), emptyList(), 0))
    val frameState: StateFlow<DomainFrame> = _frameState.asStateFlow()

    fun updateFrame(updater: DomainFrame.() -> DomainFrame ) {
        val currentFrame = _frameState.value
        val updatedFrame = updater(currentFrame)
        _frameState.value = updatedFrame
    }
    fun availablePoints(): Int {
        val balls = frameState.value.ballsList
        if (balls.isEmpty()) return 0
        val freeSize = (if (matchConfig.isFreeballEnabled) balls.size - 1 else balls.size)
        return if (freeSize <= 7) (-(8 - freeSize) * ((8 - freeSize) + 1) + 56) / 2 + (if (matchConfig.isFreeballEnabled) (9 - freeSize) else 0)
        else 27 + ((balls.size - 7) / 2) * 8 + (if (balls.size % 2 == 0) 7 else 0)
    }
    fun isFoulAndAMiss() = availablePoints() > frameState.value.scoreList.frameScoreDiff()
    fun isFrameMathematicallyOver() = availablePoints() < frameState.value.scoreList.frameScoreDiff()
    fun isLastBlackFouledOnEqualScore(potType: PotType) = potType == PotType.TYPE_FOUL && _frameState.value.ballsList.isLastBlack() && !_frameState.value.scoreList.isFrameEqual()


}