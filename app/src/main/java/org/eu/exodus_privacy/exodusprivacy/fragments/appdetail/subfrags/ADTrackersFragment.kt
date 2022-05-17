package org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.subfrags

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.FragmentADTrackersBinding

class ADTrackersFragment : Fragment(R.layout.fragment_a_d_trackers) {

    private var _binding: FragmentADTrackersBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentADTrackersBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
