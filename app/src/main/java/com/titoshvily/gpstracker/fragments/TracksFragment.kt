package com.titoshvily.gpstracker.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.titoshvily.gpstracker.MainApp
import com.titoshvily.gpstracker.MainViewModel
import com.titoshvily.gpstracker.R
import com.titoshvily.gpstracker.database.TrackAdapter
import com.titoshvily.gpstracker.database.TrackItem
import com.titoshvily.gpstracker.databinding.FragmentMainBinding
import com.titoshvily.gpstracker.databinding.TracksBinding
import com.titoshvily.gpstracker.databinding.ViewTrackBinding
import com.titoshvily.gpstracker.utils.openFragment
import kotlin.getValue


class TracksFragment() : Fragment(), TrackAdapter.Listener {
    lateinit var binding: TracksBinding
    private lateinit var adapter: TrackAdapter

    private val model : MainViewModel by activityViewModels {
        MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TracksBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
        getTracks()
    }

    private fun getTracks(){
        model.tracks.observe(viewLifecycleOwner){
            adapter.submitList(it)
            binding.textView2.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
    }



    private fun initRcView() = with(binding){
        adapter = TrackAdapter(this@TracksFragment)
        rcView.layoutManager = LinearLayoutManager(requireContext())
        rcView.adapter = adapter

    }

    override fun onClick(track: TrackItem, type: TrackAdapter.ClickType) {
    when(type){
        TrackAdapter.ClickType.DELETE -> model.deleteTrack(track)
        TrackAdapter.ClickType.OPEN ->{
model.currentTrack.value = track
         openFragment(ViewTrackFragment.newInstance())
    }}


    }


    companion object {

        @JvmStatic
        fun newInstance() = TracksFragment()
    }
}