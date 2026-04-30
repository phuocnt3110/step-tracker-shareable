package com.nphstudio.appname.ui.language

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
import com.nphstudio.appname.databinding.FragmentLanguageBinding

class LanguageFragment : Fragment() {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Populate language list from product spec
        binding.btnContinue.setOnClickListener {
            navigateToOnboarding()
        }
    }

    private fun navigateToOnboarding() {
        NphAds.showInterstitial(
            activity = requireActivity(),
            nameSpace = "nsp-interstitial-language-fullscreen-complete",
            listener = object : NphAdListener() {
                override fun onAdDismissed() {
                    if (isAdded) findNavController().navigate(R.id.action_language_to_onboarding)
                }
                override fun onAdFailed(error: AdError) {
                    if (isAdded) findNavController().navigate(R.id.action_language_to_onboarding)
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
