package com.quickpoint.snookerboard.domain.utils

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.quickpoint.snookerboard.core.utils.MatchAction
import com.quickpoint.snookerboard.data.K_BOOL_TOGGLE_FREEBALL
import com.quickpoint.snookerboard.data.K_BOOL_TOGGLE_LONG_SHOT
import com.quickpoint.snookerboard.data.K_BOOL_TOGGLE_REST_SHOT
import com.quickpoint.snookerboard.data.K_INT_MATCH_AVAILABLE_FRAMES
import com.quickpoint.snookerboard.data.K_INT_MATCH_AVAILABLE_POINTS
import com.quickpoint.snookerboard.data.K_INT_MATCH_AVAILABLE_REDS
import com.quickpoint.snookerboard.data.K_INT_MATCH_COUNTER_RETAKE
import com.quickpoint.snookerboard.data.K_INT_MATCH_CRT_PLAYER
import com.quickpoint.snookerboard.data.K_INT_MATCH_FOUL_MODIFIER
import com.quickpoint.snookerboard.data.K_INT_MATCH_HANDICAP_FRAME
import com.quickpoint.snookerboard.data.K_INT_MATCH_HANDICAP_MATCH
import com.quickpoint.snookerboard.data.K_INT_MATCH_POINTS_WITHOUT_RETURN
import com.quickpoint.snookerboard.data.K_INT_MATCH_STARTING_PLAYER
import com.quickpoint.snookerboard.data.K_INT_MATCH_UNIQUE_ID
import com.quickpoint.snookerboard.data.K_LONG_MATCH_CRT_FRAME
import com.quickpoint.snookerboard.data.K_LONG_MATCH_STATE
import com.quickpoint.snookerboard.domain.models.DomainPot
import com.quickpoint.snookerboard.domain.models.PotAction
import com.quickpoint.snookerboard.domain.models.PotDirection
import com.quickpoint.snookerboard.domain.models.PotType
import com.quickpoint.snookerboard.domain.models.listOfPotTypesPointsAdding
import com.quickpoint.snookerboard.domain.repository.DataStoreRepository
import com.quickpoint.snookerboard.domain.utils.MatchState.RULES_IDLE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.absoluteValue

@Singleton
class MatchConfigImpl @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
) : MatchConfig {
    override suspend fun loadMatchIfSaved() {
        val preferences = dataStoreRepository.getPreferences()
        matchState = getMatchStateFromOrdinal(preferences[intPreferencesKey(K_LONG_MATCH_STATE)] ?: 0)
        availableFrames = preferences[intPreferencesKey(K_INT_MATCH_AVAILABLE_FRAMES)] ?: 2
        availableReds = preferences[intPreferencesKey(K_INT_MATCH_AVAILABLE_REDS)] ?: 15
        foulModifier = preferences[intPreferencesKey(K_INT_MATCH_FOUL_MODIFIER)] ?: 0
        startingPlayer = preferences[intPreferencesKey(K_INT_MATCH_STARTING_PLAYER)] ?: -1
        handicapFrame = preferences[intPreferencesKey(K_INT_MATCH_HANDICAP_FRAME)] ?: 0
        handicapMatch = preferences[intPreferencesKey(K_INT_MATCH_HANDICAP_MATCH)] ?: 0
        crtFrame = preferences[longPreferencesKey(K_LONG_MATCH_CRT_FRAME)] ?: 0L
        crtPlayer = preferences[intPreferencesKey(K_INT_MATCH_CRT_PLAYER)] ?: -1
        maxFramePoints = preferences[intPreferencesKey(K_INT_MATCH_AVAILABLE_POINTS)] ?: 0
        counterRetake = preferences[intPreferencesKey(K_INT_MATCH_COUNTER_RETAKE)] ?: 0
        pointsWithoutReturn = preferences[intPreferencesKey(K_INT_MATCH_POINTS_WITHOUT_RETURN)] ?: 0
        isFreeballEnabled = preferences[booleanPreferencesKey(K_BOOL_TOGGLE_FREEBALL)] ?: false
        isLongShotToggleEnabled = preferences[booleanPreferencesKey(K_BOOL_TOGGLE_LONG_SHOT)] ?: false
        isRestShotToggleEnabled = preferences[booleanPreferencesKey(K_BOOL_TOGGLE_REST_SHOT)] ?: false
        Timber.i("loadPreferences(): ${getAsText()}")
    }

    override var matchState = RULES_IDLE
        set(value) {
            dataStoreRepository.savePrefs(K_LONG_MATCH_STATE, value.ordinal)
            field = value
        }

    override var availableFrames = 2
        set(value) {
            dataStoreRepository.savePrefs(K_INT_MATCH_AVAILABLE_FRAMES, value)
            field = value
        }

    override var availableReds = 15
        set(value) {
            dataStoreRepository.savePrefs(K_INT_MATCH_AVAILABLE_REDS, value)
            field = value
        }
    override var foulModifier = 0
        set(value) {
            dataStoreRepository.savePrefs(K_INT_MATCH_FOUL_MODIFIER, value)
            field = value
        }
    override var startingPlayer = -1
        set(value) {
            dataStoreRepository.savePrefs(K_INT_MATCH_STARTING_PLAYER, value)
            field = value
        }

    override var handicapFrame = 0
        set(value) {
            dataStoreRepository.savePrefs(K_INT_MATCH_HANDICAP_FRAME, value)
            field = value
        }

    override var handicapMatch = 0
        set(value) {
            dataStoreRepository.savePrefs(K_INT_MATCH_HANDICAP_MATCH, value)
            field = value
        }

    override var crtPlayer = -1
        set(value) {
            dataStoreRepository.savePrefs(K_INT_MATCH_CRT_PLAYER, value)
            field = value
        }

    override var crtFrame = 0L
        set(value) {
            dataStoreRepository.savePrefs(K_LONG_MATCH_CRT_FRAME, value)
            field = value
        }

    override var maxFramePoints = 0
        set(value) {
            dataStoreRepository.savePrefs(K_INT_MATCH_AVAILABLE_POINTS, value)
            field = value
        }

    override var counterRetake = 0
        set(value) {
            dataStoreRepository.savePrefs(K_INT_MATCH_COUNTER_RETAKE, value)
            field = value
        }
    override var pointsWithoutReturn = 0
        set(value) {
            dataStoreRepository.savePrefs(K_INT_MATCH_POINTS_WITHOUT_RETURN, value)
            field = value
        }
    override var isFreeballEnabled = false
        set(value) {
            dataStoreRepository.savePrefs(K_BOOL_TOGGLE_FREEBALL, value)
            field = value
        }

    override var isLongShotToggleEnabled = false
        set(value) {
            dataStoreRepository.savePrefs(K_BOOL_TOGGLE_LONG_SHOT, value)
            field = value
        }

    override var isRestShotToggleEnabled = false
        set(value) {
            dataStoreRepository.savePrefs(K_BOOL_TOGGLE_REST_SHOT, value)
            field = value
        }

    override fun generateUniqueId() = runBlocking {
        withContext(Dispatchers.IO) {
            dataStoreRepository.incrementAndGetUniqueId()
        }
    }

    override fun getOtherPlayer() = 1 - crtPlayer
    override fun handicapFrameExceedsLimit(key: String, value: Int) =
        key == K_INT_MATCH_HANDICAP_FRAME && (handicapFrame + value).absoluteValue >= availableReds * 8 + 27

    override fun handicapMatchExceedsLimit(key: String, value: Int) =
        key == K_INT_MATCH_HANDICAP_MATCH && (handicapMatch + value).absoluteValue == availableFrames

    override fun updateSettings(key: String, value: Int) {
        when (key) {
            K_INT_MATCH_AVAILABLE_FRAMES -> {
                availableFrames = value
                handicapMatch = 0
            }

            K_INT_MATCH_AVAILABLE_REDS -> {
                availableReds = value
                handicapFrame = 0
            }

            K_INT_MATCH_FOUL_MODIFIER -> foulModifier = value
            K_INT_MATCH_STARTING_PLAYER -> startingPlayer = if (value == 2) (0..1).random() else value
            K_INT_MATCH_HANDICAP_FRAME -> handicapFrame = if (value == 0) 0 else handicapFrame + value
            K_INT_MATCH_HANDICAP_MATCH -> handicapMatch = if (value == 0) 0 else handicapMatch + value
            else -> Timber.e("Functionality for key $key not implemented")
        }
    }

    override fun getCrtPlayerFromPotAction(potAction: PotAction) = when (potAction) {
        PotAction.SWITCH -> getOtherPlayer()
        PotAction.FIRST -> startingPlayer
        PotAction.CONTINUE, PotAction.RETAKE -> crtPlayer
    }

    override fun formatAvailableFramesForDisplay() = "(" + (availableFrames * 2 - 1).toString() + ")"
    override fun resetFrameAndGetFirstPlayer(matchAction: MatchAction) {
        if (matchAction == MatchAction.FRAME_START_NEW) {
            crtFrame += 1
            startingPlayer = 1 - startingPlayer
        }
        maxFramePoints = availableReds * 8 + 27
        crtPlayer = startingPlayer
        counterRetake = 0
        isFreeballEnabled = false
        isLongShotToggleEnabled = false
        isRestShotToggleEnabled = false
    }

    override fun getAsText() =
        "matchState: $matchState, uniqueId: ${dataStoreRepository.uniqueId}, availableFrames: $availableFrames, availableReds: $availableReds, foulModifier: $foulModifier, startingPlayer: $startingPlayer, handicapFrame: $handicapFrame, handicapMatch: $handicapMatch, crtFrame: $crtFrame, crtPlayer: $crtPlayer, availablePoints: $maxFramePoints"

    override fun resetRules(): Int {
        dataStoreRepository.savePrefs(K_INT_MATCH_UNIQUE_ID, -1)
        availableFrames = 2
        availableReds = 15
        foulModifier = 0
        startingPlayer = -1
        handicapFrame = 0
        handicapMatch = 0
        crtFrame = 1L
        crtPlayer = -1
        maxFramePoints = 0
        counterRetake = 0
        pointsWithoutReturn = 0
        return -1
    }

    override fun handlePotFreeballToggle(potType: PotType) { // Control freeball visibility and selection
        when (potType) {
            PotType.TYPE_FREE_ACTIVE -> dataStoreRepository.savePrefAndSwitchBoolValue(K_BOOL_TOGGLE_FREEBALL)
            PotType.TYPE_HIT, PotType.TYPE_FREE, PotType.TYPE_MISS, PotType.TYPE_SAFE, PotType.TYPE_SAFE_MISS, PotType.TYPE_SNOOKER, PotType.TYPE_FOUL -> dataStoreRepository.savePrefs(K_BOOL_TOGGLE_FREEBALL, false)
            PotType.TYPE_ADDRED, PotType.TYPE_REMOVE_RED, PotType.TYPE_REMOVE_COLOR, PotType.TYPE_LAST_BLACK_FOULED, PotType.TYPE_RESPOT_BLACK, PotType.TYPE_FOUL_ATTEMPT -> {}
        }
    }

    override fun handleUndoFreeballToggle(potType: PotType, lastPotType: PotType?) {
        when (potType) {
            PotType.TYPE_FREE -> dataStoreRepository.savePrefAndSwitchBoolValue(K_BOOL_TOGGLE_FREEBALL)
            PotType.TYPE_FREE_ACTIVE -> dataStoreRepository.savePrefs(K_BOOL_TOGGLE_FREEBALL, false)
            PotType.TYPE_SAFE, PotType.TYPE_MISS, PotType.TYPE_SAFE_MISS, PotType.TYPE_SNOOKER, PotType.TYPE_FOUL -> when (lastPotType) {
                PotType.TYPE_FREE_ACTIVE -> dataStoreRepository.savePrefAndSwitchBoolValue(K_BOOL_TOGGLE_FREEBALL)
                else -> dataStoreRepository.savePrefs(K_BOOL_TOGGLE_FREEBALL, false)
            }
            PotType.TYPE_HIT, PotType.TYPE_ADDRED, PotType.TYPE_REMOVE_RED, PotType.TYPE_REMOVE_COLOR, PotType.TYPE_LAST_BLACK_FOULED, PotType.TYPE_RESPOT_BLACK, PotType.TYPE_FOUL_ATTEMPT -> {}
        }
    }

    override fun calculatePointsWithoutReturn(pot: DomainPot, potDirection: PotDirection): Int {
        val points = if (potDirection == PotDirection.POT) 1 else -1
        if (pot.potType in listOfPotTypesPointsAdding) {
            pointsWithoutReturn = when {
                pointsWithoutReturn > 0 -> if (crtPlayer == 1) pointsWithoutReturn + points else - points
                pointsWithoutReturn == 0 -> if (crtPlayer == 1) points else - points
                pointsWithoutReturn < 0 -> if (crtPlayer == 1) points else pointsWithoutReturn - points
                else -> throw IllegalStateException("Invalid pointsWithoutReturn state")
            }
        }
        return pointsWithoutReturn
    }

    override fun updateCounterRetake(pot: DomainPot?, potDirection: PotDirection) {
        when (potDirection) {
            PotDirection.POT -> {
                pot?.let {
                    if (
                        pot.potAction == PotAction.RETAKE || (pot.potType == PotType.TYPE_FOUL && pot.potAction == PotAction.CONTINUE && counterRetake == 2))
                        counterRetake += 1 else counterRetake = 0
                }
            }

            PotDirection.UNDO -> if (counterRetake >= 0) counterRetake--
        }
    }

    override fun isVisibleBallFouledThreeTimes() = counterRetake == 3
}
