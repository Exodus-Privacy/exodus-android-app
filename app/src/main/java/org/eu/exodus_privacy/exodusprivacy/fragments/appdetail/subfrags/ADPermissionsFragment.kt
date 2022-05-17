package org.eu.exodus_privacy.exodusprivacy.fragments.appdetail.subfrags

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import org.eu.exodus_privacy.exodusprivacy.R
import org.eu.exodus_privacy.exodusprivacy.databinding.FragmentADPermissionsBinding

@AndroidEntryPoint
class ADPermissionsFragment : Fragment(R.layout.fragment_a_d_permissions) {

    private var _binding: FragmentADPermissionsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentADPermissionsBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
