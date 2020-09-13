package org.persianbms.andromeda


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.persianbms.andromeda.databinding.FragmentHomeBinding

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment(), MainActivity.BackPressInterceptor {

    private var binding: FragmentHomeBinding? = null
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is MainActivity) {
            context.backPressInterceptor = this
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = DataBindingUtil.inflate<FragmentHomeBinding>(inflater, R.layout.fragment_home, container, false)
        val settings = binding!!.webView.settings
        settings.javaScriptEnabled = true
        settings.databaseEnabled = true
        settings.domStorageEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.safeBrowsingEnabled = false
        }
        binding.webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        binding.webView.webViewClient = PBMSViewClient()
        binding.webView.webChromeClient = PBMSChromeClient()

        // set up buttons
        binding.navigateBack.setOnClickListener { onNavigateBack() }
        binding.navigateForward.setOnClickListener { onNavigateForward() }
        binding.reloadPage.setOnClickListener { onReloadPage() }
        binding.navigateHome.setOnClickListener { onNavigateHome() }

        val activity = requireActivity() as MainActivity
        val webBundle = activity.webBundle
        if (webBundle != null) {
            binding.webView.restoreState(webBundle)
            updateNavButtons()
        } else {
            binding.webView.loadUrl(Constants.SITE_URL)
        }

        this.binding = binding

        return binding.root
    }

    override fun onDestroyView() {
        binding = null

        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()

        binding?.webView?.onResume()

        val activity = requireActivity() as MainActivity
        activity.setFragmentMenu(R.menu.main)
    }

    override fun onStop() {
        super.onStop()

        binding?.let { bind ->
            bind.webView.onPause()
            activity?.let { main ->
                if (main is MainActivity) {
                    val webState = Bundle()
                    bind.webView.saveState(webState)
                    main.webBundle = webState
                }

            }
        }
    }

    private fun updateNavButtons() {
        binding?.let { bind ->
            bind.pageProgressIndicator.visibility = View.VISIBLE
            bind.navigateBack.isEnabled = bind.webView.canGoBack()
            bind.navigateForward.isEnabled = bind.webView.canGoForward()
        }
    }

    //region navigation clicks

    private fun onNavigateBack() {
        binding?.webView?.goBack()
    }

    private fun onNavigateForward() {
        binding?.webView?.goForward()
    }

    private fun onNavigateHome() {
        binding?.webView?.loadUrl(Constants.SITE_URL)
    }

    private fun onReloadPage() {
        binding?.webView?.reload()
    }

    //endregion

    //region BackPressInterceptor

    override fun shouldInterceptBackPress(): Boolean {
        val binding = binding ?: return false

        // Are we showing a full screen video?
        if (customView != null && customViewCallback != null) {
            val root = requireActivity().findViewById<ConstraintLayout>(R.id.root)
            root.removeView(customView)
            customViewCallback!!.onCustomViewHidden()

            customView = null
            customViewCallback = null

            return true
        }

        // we must not be showing a video, so handle it as a back navigation
        if (binding.webView.canGoBack()) {
            onNavigateBack()
            return true
        }

        // we're at the beginning of our url history, so let the activity handle the back press
        return false
    }

    //endregion

    private inner class PBMSViewClient : android.webkit.WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            updateNavButtons()
            binding?.pageProgressIndicator?.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)

            updateNavButtons()
            binding?.pageProgressIndicator?.visibility = View.INVISIBLE
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            // authority should never be null, but let's be safe.
            val authority = request.url.authority ?: return false
            if (authority == Constants.PBMS_AUTHORITY) {
                return false
            }

            // this url is off-site, so override it
            val i = Intent(Intent.ACTION_VIEW)
            i.data = request.url
            startActivity(i)

            return true
        }
    }

    private inner class PBMSChromeClient : android.webkit.WebChromeClient() {
        override fun onHideCustomView() {
            if (customView == null) {
                return
            }

            if (binding == null) {
                return
            }

            val activity = activity ?: return

            val root = activity.findViewById<ConstraintLayout>(R.id.root)
            root.removeView(customView)

            customView = null
            customViewCallback = null
        }

        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
            // check if we can even display the view
            val activity = activity
            if (binding == null || activity == null) {
                callback.onCustomViewHidden()
                return
            }

            customView = view
            customViewCallback = callback
            val root = activity.findViewById<ConstraintLayout>(R.id.root)
            root.addView(view,
                ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            )
        }
    }
}
