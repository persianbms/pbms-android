package org.persianbms.andromeda;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.persianbms.andromeda.databinding.FragmentHomeBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    @Nullable private FragmentHomeBinding binding;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        WebSettings settings = binding.webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSaveFormData(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(false);
        }
        binding.webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        binding.webView.setWebViewClient(new WebViewClient());

        if (savedInstanceState != null) {
            binding.webView.restoreState(savedInstanceState);
        } else {
            binding.webView.loadUrl("https://persianbahaimedia.org/");
        }

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        binding = null;

        super.onDestroyView();
    }

    @Override
    public void onPause() {
        if (binding != null) {
            binding.webView.onPause();
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (binding != null) {
            binding.webView.onResume();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (binding != null) {
            binding.webView.saveState(outState);
        }

        super.onSaveInstanceState(outState);
    }

    private class WebViewClient extends android.webkit.WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            if (binding != null) {
                binding.pageProgressIndicator.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            if (binding != null) {
                binding.pageProgressIndicator.setVisibility(View.INVISIBLE);
            }
        }
    }
}
