package com.jorkoh.transportezaragozakt.destinations.scanner

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.zxing.integration.android.IntentIntegrator

class ScannerFragment : Fragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        IntentIntegrator.forSupportFragment(this).initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            val toastText = if (result.contents == null) {
                "Cancelled from fragment"
            } else {
                "Scanned from fragment: " + result.contents
            }
            Toast.makeText(activity, toastText, Toast.LENGTH_LONG).show()
        }
        fragmentManager?.popBackStack()
    }
}