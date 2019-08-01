package com.jorkoh.transportezaragozakt.destinations.web_view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.jorkoh.transportezaragozakt.R
import kotlinx.android.synthetic.main.web_view_destination.view.*

class WebViewFragment : Fragment() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.web_view_destination, container, false).apply {
            val args = WebViewFragmentArgs.fromBundle(requireArguments())


            web_view.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    view?.loadUrl(request?.url.toString())
                    return true
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    web_view.visibility = View.INVISIBLE
                    loading_web_view_animation.visibility = View.VISIBLE
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    if (args.javascript != null) {
                        view?.evaluateJavascript(args.javascript) {
                            view.visibility = View.VISIBLE
                            loading_web_view_animation.visibility = View.INVISIBLE
                        }
                    } else {
                        view?.visibility = View.VISIBLE
                        loading_web_view_animation.visibility = View.INVISIBLE
                    }
                }
            }
            web_view.settings.javaScriptEnabled = true
            web_view.settings.setAppCacheEnabled(true)

            if (args.isTwitterTimeline) {
                web_view.loadDataWithBaseURL(
                    "https://twitter.com",
                    "<a class=\"twitter-timeline\" href=\"${args.url}\"></a> <script async src=\"https://platform.twitter.com/widgets.js\" charset=\"utf-8\"></script>",
                    "text/html",
                    "utf-8",
                    ""
                )
            } else {
                web_view.loadUrl(args.url)
            }
        }
    }
}