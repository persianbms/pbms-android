package org.persianbms.andromeda;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.jetbrains.annotations.NotNull;
import org.persianbms.andromeda.databinding.FragmentHomeBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements MainActivity.BackPressInterceptor {

    @Nullable private FragmentHomeBinding binding;
    @Nullable private View customView;
    @Nullable private WebChromeClient.CustomViewCallback customViewCallback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity)context;
            activity.setBackPressInterceptor(this);
        }
    }

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
        binding.webView.setWebViewClient(new PBMSViewClient());
        binding.webView.setWebChromeClient(new PBMSChromeClient());

        // set up buttons
        binding.navigateBack.setOnClickListener(view -> onNavigateBack());
        binding.navigateForward.setOnClickListener(view -> onNavigateForward());
        binding.reloadPage.setOnClickListener(view -> onReloadPage());
        binding.navigateHome.setOnClickListener(view -> onNavigateHome());

        MainActivity activity = (MainActivity) requireActivity();
        if (activity.getWebBundle() != null) {
            binding.webView.restoreState(activity.getWebBundle());
            updateNavButtons();
        } else {
            binding.webView.loadUrl(Constants.SITE_URL);
        }

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        binding = null;

        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (binding != null) {
            binding.webView.onResume();
        }

        MainActivity activity = (MainActivity) requireActivity();
        activity.setFragmentMenu(R.menu.main);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (binding != null) {
            binding.webView.onPause();
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                Bundle webState = new Bundle();
                binding.webView.saveState(webState);
                activity.setWebBundle(webState);
            }
        }
    }

    private void updateNavButtons() {
        if (binding != null) {
            binding.pageProgressIndicator.setVisibility(View.VISIBLE);
            binding.navigateBack.setEnabled(binding.webView.canGoBack());
            binding.navigateForward.setEnabled(binding.webView.canGoForward());
        }
    }

    //region navigation clicks

    private void onNavigateBack() {
        if (binding == null) {
            return;
        }

        binding.webView.goBack();
    }

    private void onNavigateForward() {
        if (binding == null) {
            return;
        }

        binding.webView.goForward();
    }

    private void onNavigateHome() {
        if (binding == null) {
            return;
        }

        binding.webView.loadUrl(Constants.SITE_URL);
    }

    private void onReloadPage() {
        if (binding == null) {
            return;
        }

        binding.webView.reload();
    }

    //endregion

    //region BackPressInterceptor

    @Override
    public boolean shouldInterceptBackPress() {
        if (binding == null) {
            return false;
        }

        // Are we showing a full screen video?
        if (customView != null && customViewCallback != null) {
            ConstraintLayout root = requireActivity().findViewById(R.id.root);
            root.removeView(customView);
            customViewCallback.onCustomViewHidden();

            customView = null;
            customViewCallback = null;

            return true;
        }

        if (binding.webView.canGoBack()) {
            onNavigateBack();
            return true;
        }

        return false;
    }

    //endregion

    private class PBMSViewClient extends android.webkit.WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            updateNavButtons();
            if (binding != null) {
                binding.pageProgressIndicator.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            updateNavButtons();
            if (binding != null) {
                binding.pageProgressIndicator.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String authority = request.getUrl().getAuthority();
            if (authority == null) {
                // This shouldn't actually happen, but let's be safe.
                return false;
            }
            if (authority.equals(Constants.PBMS_AUTHORITY)) {
                return false;
            }

            // this url is off-site, so override it
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(request.getUrl());
            startActivity(i);

            return true;
        }
    }

    private class PBMSChromeClient extends android.webkit.WebChromeClient {
        @Override
        public void onHideCustomView() {
            if (customView == null) {
                return;
            }

            if (binding == null) {
                return;
            }

            FragmentActivity activity = getActivity();
            if (activity == null) {
                return;
            }

            ConstraintLayout root = activity.findViewById(R.id.root);
            root.removeView(customView);

            customView = null;
            customViewCallback = null;
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            // check if we can even display the view
            FragmentActivity activity = getActivity();
            if (binding == null || activity == null) {
                callback.onCustomViewHidden();
                return;
            }

            customView = view;
            customViewCallback = callback;
            ConstraintLayout root = activity.findViewById(R.id.root);
            root.addView(view, new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }
}
