package com.quickpoint.snookerboard.fragments.gamedialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.quickpoint.snookerboard.DialogViewModel
import com.quickpoint.snookerboard.MatchViewModel
import com.quickpoint.snookerboard.R
import com.quickpoint.snookerboard.databinding.FragmentDialogGenBinding
import com.quickpoint.snookerboard.fragments.game.GameViewModel
import com.quickpoint.snookerboard.utils.*
import com.quickpoint.snookerboard.utils.MatchAction.*
import timber.log.Timber


class GenericDialogFragment : DialogFragment() {
    private val dialogVm: DialogViewModel by activityViewModels()
    private lateinit var gameVm: GameViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayoutSizeByFactor(resources.getDimension(R.dimen.dialog_factor))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding: FragmentDialogGenBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_dialog_gen, container, false)
        gameVm = ViewModelProvider(requireParentFragment().childFragmentManager.fragments[0])[GameViewModel::class.java]

        // Bind all required elements from the view
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            varDialogVm = dialogVm
            varGameVm = this@GenericDialogFragment.gameVm
            GenericDialogFragmentArgs.fromBundle(requireArguments()).apply {
                varDialogMatchActionA = matchActionA
                varDialogMatchActionB = if (matchActionC == MATCH_TO_END) MATCH_ENDED_DISCARD_FRAME else CLOSE_DIALOG
                varDialogMatchActionC = matchActionC
                if (varDialogMatchActionC in listOfMatchActionsUncancelable) {
                    this@GenericDialogFragment.isCancelable = false // An action has to be taken if game or match are ended
                }
            }
        }

        // Observers
        dialogVm.eventDialogAction.observe(viewLifecycleOwner, EventObserver { action ->
            gameVm.onEventGameAction(action, when(action) {
                MATCH_CANCEL, FRAME_RERACK, FRAME_START_NEW ->  true
                else -> false
            })
            dismiss() // Close dialog once a match action as been clicked on
        })
        return binding.root
    }
}

