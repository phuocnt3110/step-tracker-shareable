package com.nphstudio.appname.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nphlab.sdk.ads.NphAds
import com.nphstudio.appname.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load banner ad
        NphAds.loadBannerInto(binding.adBannerContainer, "nsp-banner-settings-bottom-auto")

        // TODO: Populate settings items from product spec

        binding.btnPremium.setOnClickListener {
            // TODO: Launch IAP flow
            // On success: NphAds.setPremium(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
