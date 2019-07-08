package com.jorkoh.transportezaragozakt.destinations.more

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.jorkoh.transportezaragozakt.R
import kotlinx.android.synthetic.main.main_container.*


class MoreFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        // Themes
        findPreference<Preference>(getString(R.string.themes_key))?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            findNavController().navigate(MoreFragmentDirections.actionMoreToThemePicker())
            true
        }

        // Recharge card
        findPreference<Preference>(getString(R.string.recharge_card_key))?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://recargazaragoza.avanzagrupo.com")))
            true
        }

        // Avanza Twitter
        findPreference<Preference>(getString(R.string.avanza_twitter_key))?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/buszaragoza")))
            true
        }

        // Feedback
        findPreference<Preference>(getString(R.string.feedback_key))?.onPreferenceClickListener = Preference.OnPreferenceClickListener{
            composeFeedbackEmail()
            true
        }

        // Privacy policy
        findPreference<Preference>(getString(R.string.privacy_policy_key))?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://jorkoh.github.io/TransporteZaragoza/PrivacyPolicy")))
            true
        }

        // Libraries and credits
        findPreference<Preference>(getString(R.string.libraries_and_credits_key))?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            MaterialDialog(requireContext()).show {
                title(R.string.libraries_and_credits_title)
                message(R.string.libraries_and_credits_message) {
                    html()
                }
            }
            true
        }

        // Open source
        findPreference<Preference>(getString(R.string.open_source_key))?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Jorkoh/TransporteZaragozaKT")))
            true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setupToolbar()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun setupToolbar() {
        requireActivity().main_toolbar.menu.apply {
            (findItem(R.id.item_search)?.actionView as SearchView?)?.setOnQueryTextListener(null)
            clear()
        }
    }

    private fun composeFeedbackEmail() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.feedback_message_address)))
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_message_subject))
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.feedback_message_text))
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }
}
