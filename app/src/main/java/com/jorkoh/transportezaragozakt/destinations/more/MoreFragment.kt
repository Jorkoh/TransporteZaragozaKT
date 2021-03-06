package com.jorkoh.transportezaragozakt.destinations.more

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import com.afollestad.materialdialogs.MaterialDialog
import com.jorkoh.transportezaragozakt.R
import com.jorkoh.transportezaragozakt.destinations.utils.PreferenceFragmentCompatWithToolbar
import com.jorkoh.transportezaragozakt.destinations.utils.isSpanish
import com.jorkoh.transportezaragozakt.destinations.utils.toPx


class MoreFragment : PreferenceFragmentCompatWithToolbar() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        // Themes
        findPreference<Preference>(getString(R.string.themes_key))?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            findNavController().navigate(MoreFragmentDirections.actionMoreToThemePicker())
            true
        }

        // Recharge card
        findPreference<Preference>(getString(R.string.recharge_card_key))?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://recargazaragoza.avanzagrupo.com")))
                true
            }

        // Tram Twitter
        findPreference<Preference>(getString(R.string.tram_twitter_key))?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                findNavController().navigate(
                    MoreFragmentDirections.actionMoreToWebView(
                        url = "https://twitter.com/tranviadezgz",
                        title = getString(R.string.tram_twitter_title),
                        isTwitterTimeline = true
                    )
                )
                true
            }

        // Bus Twitter
        findPreference<Preference>(getString(R.string.bus_twitter_key))?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                findNavController().navigate(
                    MoreFragmentDirections.actionMoreToWebView(
                        url = "https://twitter.com/buszaragoza",
                        title = getString(R.string.bus_twitter_title),
                        isTwitterTimeline = true
                    )
                )
                true
            }

        // Rural Twitter
        findPreference<Preference>(getString(R.string.rural_twitter_key))?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                findNavController().navigate(
                    MoreFragmentDirections.actionMoreToWebView(
                        url = "https://twitter.com/ctazgz",
                        title = getString(R.string.rural_twitter_title),
                        isTwitterTimeline = true
                    )
                )
                true
            }

        // Feedback
        findPreference<Preference>(getString(R.string.feedback_key))?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            composeFeedbackEmail()
            true
        }

        // Privacy policy
        findPreference<Preference>(getString(R.string.privacy_policy_key))?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                findNavController().navigate(
                    MoreFragmentDirections.actionMoreToWebView(
                        url = "https://jorkoh.github.io/TransporteZaragoza/PrivacyPolicy",
                        title = getString(R.string.privacy_policy_title)
                    )
                )
                true
            }

        // Open source
        findPreference<Preference>(getString(R.string.open_source_key))?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Jorkoh/TransporteZaragozaKT")))
            true
        }
    }

    override fun onStart() {
        super.onStart()

        // Changelog
        val savedChangelogKey = getString(if (requireContext().isSpanish()) R.string.saved_changelog_es_key else R.string.saved_changelog_en_key)
        val changelog = android.preference.PreferenceManager.getDefaultSharedPreferences(context).getString(savedChangelogKey, "")
        findPreference<Preference>(getString(R.string.changelog_key))?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                MaterialDialog(requireContext()).show {
                    title(R.string.changelog_title)
                    message(text = changelog ?: "")
                }
                true
            }
        handleDeepLinks()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.setPadding(0, 0, 0, 56.toPx())
    }

    private fun composeFeedbackEmail() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.feedback_message_address)))
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_message_subject))
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.feedback_message_text))
        intent.resolveActivity(requireActivity().packageManager)?.run {
            startActivity(intent)
        }
    }

    // Handles opening stops information directly from notifications or shortcuts
    private fun handleDeepLinks() {
        // Jetpack's Navigation doesn't quite work with the multi-graph setup
        // required for bottom navigation with multi-stack so it's handled manually here
        requireActivity().intent = requireActivity().intent.apply {
            dataString?.let { uri ->
                if (uri == "launchTZ://viewChangelog/") {
                    findPreference<Preference>(getString(R.string.changelog_key))?.performClick()
                }
            }
        }
    }
}
