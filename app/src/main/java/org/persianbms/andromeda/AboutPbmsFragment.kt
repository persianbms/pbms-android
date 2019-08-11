package org.persianbms.andromeda

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import java.lang.StringBuilder

class AboutPbmsFragment : Fragment() {

    private var webView: WebView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val wv = WebView(requireContext())

        val settings = wv.settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.safeBrowsingEnabled = false
        }
        wv.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        val reader = resources.openRawResource(R.raw.about_us_fa).bufferedReader()
        val html = StringBuilder()
        var line: String?
        do {
            line = reader.readLine()
            if (line != null) {
                html.append(line)
            }
        } while (line != null)
        wv.loadData(html.toString(), "text/html", "utf-8")

        webView = wv

        return wv
    }
}