package com.quickpoint.snookerboard.domain.models

import com.quickpoint.snookerboard.domain.utils.MatchConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DomainBreakManager @Inject constructor(
    private val matchConfig: MatchConfig,
) {
    private val _breakListFlow = MutableStateFlow<List<DomainBreak>>(emptyList())
    val breakListFlow: StateFlow<List<DomainBreak>> = _breakListFlow.asStateFlow()

    fun onPot(pot: DomainPot) {
        if (isFreeBallMissedAfterFreeBallMissed(pot.potType)) {
            removeLastPotFromBreaksList()
        } else {
            addToBreaksList(pot)
        }
    }

    private fun addToBreaksList(pot: DomainPot) {
        val crtBreakList = _breakListFlow.value.toMutableList()

        val shouldCreateNewBreak = crtBreakList.isEmpty() // Add a new crt break if there are no breaks
                || pot.potType !in listOfPotTypesPointsAdding // or if the crt pot is not generating points
                || crtBreakList.lastPotType() !in listOfPotTypesPointsAdding // or if previous pot was not generating points
                || crtBreakList.lastOrNull()?.player != matchConfig.crtPlayer // or if player has changed

        if (shouldCreateNewBreak) {
            crtBreakList.add(
                DomainBreak(
                    matchConfig.generateUniqueId(),
                    matchConfig.crtPlayer,
                    matchConfig.crtFrame,
                    mutableListOf(),
                    0,
                    0
                )
            )
        }

        val lastBreak = crtBreakList.last()
        crtBreakList[crtBreakList.lastIndex] = lastBreak.copy(
            pots = lastBreak.pots.toMutableList().apply { add(pot) },
            breakSize = if (pot.potType in listOfPotTypesPointsAdding) lastBreak.breakSize + pot.ball.points else lastBreak.breakSize,
        )
        updateBreaksList(crtBreakList)
    }

    fun removeLastPotFromBreaksList(): DomainPot {
        val crtBreakList = _breakListFlow.value.toMutableList()
        val lastBreak = crtBreakList.last()
        val removedPot = lastBreak.pots.removeLast() // Remove the last pot

        if (removedPot.potType in listOfPotTypesPointsAdding) {
            crtBreakList[crtBreakList.lastIndex] = lastBreak.copy(
                pots = lastBreak.pots,
                breakSize = lastBreak.breakSize - removedPot.ball.points,
            )
        }
        crtBreakList.removeAll { it.pots.isEmpty() }
        updateBreaksList(crtBreakList)

        return removedPot
    }

    fun updateBreaksList(crtBreakList: List<DomainBreak>) {
        _breakListFlow.value = crtBreakList
    }

    fun findMaxBreak() = _breakListFlow.value.filter { it.player == matchConfig.crtPlayer }.maxOfOrNull { it.breakSize } ?: 0

    private fun isFreeBallMissedAfterFreeBallMissed(potType: PotType) = // not sure what this is used for
        potType == PotType.TYPE_FREE_ACTIVE && _breakListFlow.value.lastPotType() == PotType.TYPE_FREE_ACTIVE
}