package com.example.snookerscore.fragments.play

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.snookerscore.GenericViewModelFactory
import com.example.snookerscore.R
import com.example.snookerscore.databinding.FragmentPlayBinding
import com.example.snookerscore.fragments.game.GameFragmentViewModel
import com.example.snookerscore.utils.EventObserver

class PlayFragment : Fragment() {

    private val playFragmentViewModel: PlayFragmentViewModel by viewModels()
    private val gameFragmentViewModel: GameFragmentViewModel by activityViewModels {
        GenericViewModelFactory(requireNotNull(this.activity).application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentPlayBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_play, container, false)

        binding.apply {
            viewModel = playFragmentViewModel
            numberPicker.apply {
                minValue = 1
                maxValue = 19
                value = 2
                displayedValues = (minValue until maxValue * 2).filter { it % 2 != 0 }.map { it.toString() }.toTypedArray()
            }

            playFragmentViewModel.apply {
                eventReds.observe(viewLifecycleOwner, EventObserver {
                    fragPlayBtnRedsSix.isSelected = it == 6
                    fragPlayBtnRedsTen.isSelected = it == 10
                    fragPlayBtnRedsFifteen.isSelected = it == 15
                })
                eventFoulModifier.observe(viewLifecycleOwner, EventObserver {
                    fragPlayBtnFoulOne.isSelected = it == -3
                    fragPlayBtnFoulTwo.isSelected = it == -2
                    fragPlayBtnFoulThree.isSelected = it == -1
                    fragPlayBtnFoulFour.isSelected = it == 0
                })
                eventBreaksFirst.observe(viewLifecycleOwner, EventObserver {
                    fragPlayBtnBreakPlayerA.isSelected = it == 0
                    fragPlayBtnBreakPlayerB.isSelected = it == 1
                })
            }

            fragPlayBtnPlay.setOnClickListener {
                gameFragmentViewModel.setMatchRules(
                    viewModel!!.eventFrames.value!!.peekContent(),
                    viewModel!!.eventReds.value!!.peekContent(),
                    viewModel!!.eventFoulModifier.value!!.peekContent(),
                    viewModel!!.eventBreaksFirst.value!!.peekContent()
                )
                it.findNavController().navigate(
                    PlayFragmentDirections.actionPlayFragmentToGameFragment(
                        viewModel!!.eventFrames.value!!.peekContent(),
                        viewModel!!.eventReds.value!!.peekContent(),
                        viewModel!!.eventFoulModifier.value!!.peekContent(),
                        viewModel!!.eventBreaksFirst.value!!.peekContent()
                    )
                )
            }
        }
        return binding.root
    }
}