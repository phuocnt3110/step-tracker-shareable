package com.nphstudio.appname.ui.onboarding

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
import com.nphstudio.appname.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        NphAds.loadBannerInto(binding.adBannerContainer, "nsp-banner-onboarding-bottom-auto")

        binding.btnGetStarted.setOnClickListener {
            navigateToHome()
        }
    }

    private fun navigateToHome() {
        NphAds.showInterstitial(
            activity = requireActivity(),
            nameSpace = "nsp-interstitial-onboarding-fullscreen-complete",
            listener = object : NphAdListener() {
                override fun onAdDismissed() {
                    if (isAdded) findNavController().navigate(R.id.action_onboarding_to_home)
                }
                override fun onAdFailed(error: AdError) {
                    if (isAdded) findNavController().navigate(R.id.action_onboarding_to_home)
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
