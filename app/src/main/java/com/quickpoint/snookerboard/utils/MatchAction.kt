package com.quickpoint.snookerboard.utils

import com.quickpoint.snookerboard.domain.PotType
import com.quickpoint.snookerboard.domain.PotType.*
import com.quickpoint.snookerboard.utils.MatchAction.*
import timber.log.Timber

enum class MatchAction {
    // Foul Actions
    FOUL_QUERY, // Attempt to submit a foul
    FOUL_CONFIRM, // Foul has been confirmed and will be processed
    FOUL_DIALOG, // Open foul dialog fragment

    // Frame Actions
    FRAME_START_NEW, // When actioned from endFrameOrMatch method in matchVm
    FRAME_RERACK_DIALOG, // On clicking rerack button
    FRAME_RERACK, // Action to reset frame
    FRAME_MISS_FORFEIT_DIALOG, // Warn player that the next miss will result in losing frame
    FRAME_MISS_FORFEIT, // Person who missed 3 times in a row loses frame if he was not snookered
    FRAME_ENDING_DIALOG, // Open frame end dialog
    FRAME_TO_END, // On clicking the concede frame button while the frame is still ongoing
    FRAME_ENDED, // Frame end has been confirmed and will be processed. On clicking the concede frame button when the point difference is big enough, or automatically triggered when only one ball left
    FRAME_RESPOT_BLACK_DIALOG, // When both players are tied at the end of the frame
    FRAME_FREE_AVAILABLE, // After a foul, uses observer to handle pot instead of directly from gameVm
    FRAME_RESPOT_BLACK, // After the RESPOT_BLACK_DIALOG is closed, respot black
    FRAME_UPDATED, // When frame updates are completed assign frame action that triggers the matchVm ot update DisplayScore
    FRAME_UNDO, // When triggered from gameVm execute as an action instead of recursive method
    FRAME_LOG_ACTIONS_DIALOG, // Opens a dialog allowing users to submit an action log
    FRAME_LOG_ACTIONS, // Submit an action log by e-mail to be tested and reviewed

    // Match Actions
    MATCH_PLAY, // When actioned from the play fragment
    MATCH_START_NEW, // When actioned from the game fragment, if no match exists in the db
    MATCH_CANCEL_DIALOG, // On clicking cancel match
    MATCH_CANCEL, // Action to cancel match
    MATCH_ENDING_DIALOG, // Open match ending dialog
    MATCH_ENDED_DISCARD_FRAME, // On clicking the conceding button when keeping/discarding current score can affect winner
    MATCH_TO_END, // On clicking the concede match button while the match is still in play
    MATCH_ENDED, // Match end has been confirmed and will be processed. On clicking the concede frame / match button after the frame is mathematically complete and if it is enough to win the game

    // Other
    NAV_TO_PLAY, // Go to main menu
    NAV_TO_POST_MATCH, // Last frame has been saved to repo so it's save to navigate to FragmentPostGame
    INFO_FOUL_DIALOG, // On clicking the info foul button on the rules screen, it will open a generic dialog

    // Redundant Actions
    CLOSE_DIALOG, // Used when the action is to continue current state
    IGNORE, // Used when action should not be shown
    TRANSITION_TO_FRAGMENT, // Action to transition to fragment in a queue

    // Snackbars
    SNACKBAR_NO_BALL, // Assign snackbar when there are no balls on the table instead handling pot
    SNACKBAR_NO_PLAYER, // Assign snackbar when player names are not fully completed
    SNACKBAR_NO_FIRST, // Assign snackbar when no first player is selected
}

val listOfMatchActionsUncancelable = listOf(MATCH_ENDED, FRAME_ENDED, FRAME_RESPOT_BLACK_DIALOG)


// Helper functions
fun MatchAction.getListOfDialogActions(isMatchEnding: Boolean, isFrameMathematicallyOver: Boolean): List<MatchAction> = when (this) {
    FRAME_RESPOT_BLACK_DIALOG -> listOf(IGNORE, IGNORE, FRAME_RESPOT_BLACK)
    FRAME_RERACK_DIALOG -> listOf(CLOSE_DIALOG, IGNORE, FRAME_RERACK)
    FRAME_ENDING_DIALOG, MATCH_ENDING_DIALOG -> listOf(CLOSE_DIALOG, IGNORE, queryEndFrameOrMatch(isMatchEnding, isFrameMathematicallyOver))
    MATCH_CANCEL_DIALOG -> listOf(CLOSE_DIALOG, IGNORE, MATCH_CANCEL)
    FRAME_LOG_ACTIONS_DIALOG -> listOf(CLOSE_DIALOG, IGNORE, FRAME_LOG_ACTIONS)
    else -> listOf()
}

fun MatchAction.queryEndFrameOrMatch(isMatchEnding: Boolean, isFrameMathematicallyOver: Boolean): MatchAction { // When actioned from options menu or if last ball has been potted
    Timber.i("queryEndFrameOrMatch($this)")
    return when { // Else assign a match action for a MATCH end query or else assign a FRAME ending action
        isMatchEnding -> { // If the frame would push player to win, assign a MATCH ending action
            if (isFrameMathematicallyOver) MATCH_ENDED
            else MATCH_TO_END
        }
        this == MATCH_ENDING_DIALOG -> MATCH_TO_END // Else assign a match action for a MATCH end query
        isFrameMathematicallyOver -> FRAME_ENDED // Else assign a FRAME ending action
        else -> FRAME_TO_END
    }
}

fun MatchAction.getPotType(): PotType? = when (this) {
    FRAME_RESPOT_BLACK -> TYPE_RESPOT_BLACK
    FRAME_FREE_AVAILABLE -> TYPE_FREE_AVAILABLE
    FOUL_CONFIRM -> TYPE_FOUL
    else -> null // For FRAME_UNDO
}