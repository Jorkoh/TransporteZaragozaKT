package com.jorkoh.transportezaragozakt.destinations.more

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
        findPreference<Preference>(getString(R.string.themes_key))?.setOnPreferenceClickListener {
            findNavController().navigate(MoreFragmentDirections.actionMoreToThemePicker())
            true
        }
        findPreference<Preference>(getString(R.string.recharge_card_key))?.setOnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://recargazaragoza.avanzagrupo.com")))
            true
        }
        findPreference<Preference>(getString(R.string.avanza_twitter_key))?.setOnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/buszaragoza")))
            true
        }

        findPreference<Preference>(getString(R.string.libraries_and_credits_key))?.setOnPreferenceClickListener {
            MaterialDialog(requireContext()).show {
                title(R.string.libraries_and_credits_title)
                message(R.string.libraries_and_credits_message){
                    html()
                }
            }
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
}
