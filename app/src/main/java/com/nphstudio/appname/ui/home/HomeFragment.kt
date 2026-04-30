package com.nphstudio.appname.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nphlab.sdk.ads.AdError
import com.nphlab.sdk.ads.NphAds
import com.nphlab.sdk.ads.listener.NphAdListener
import com.nphstudio.appname.R
import com.nphstudio.appname.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load banner ad
        NphAds.loadBannerInto(binding.adBannerContainer, "nsp-banner-home-bottom-auto")

        // Preload interstitial for settings navigation
        NphAds.preload(requireActivity(), "nsp-interstitial-home-fullscreen-clickSettings")

        // TODO: Set up main app content from product spec

        binding.btnSettings.setOnClickListener {
            navigateToSettings()
        }
    }

    private fun navigateToSettings() {
        NphAds.showInterstitial(
            activity = requireActivity(),
            nameSpace = "nsp-interstitial-home-fullscreen-clickSettings",
            listener = object : NphAdListener() {
                override fun onAdDismissed() {
                    if (isAdded) findNavController().navigate(R.id.action_home_to_settings)
                }
                override fun onAdFailed(error: AdError) {
                    if (isAdded) findNavController().navigate(R.id.action_home_to_settings)
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
