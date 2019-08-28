package org.persianbms.andromeda;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.button.MaterialButton;

import org.persianbms.andromeda.viewmodels.MainViewModel;

public class LiveStreamFragment extends Fragment {

    private MaterialButton toggle;
    private MainViewModel mainViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainViewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        toggle = (MaterialButton) inflater.inflate(R.layout.fragment_live_stream, container, false);
        toggle.setOnClickListener(view -> {
            Intent i;
            Boolean isPlaying = mainViewModel.getLiveStreamPlaying().getValue();
            if (isPlaying == null || !isPlaying) {
                i = LiveStreamService.newPlayIntent(requireContext());
            } else {
                i = LiveStreamService.newPauseIntent(requireContext());
            }
            requireActivity().startService(i);
        });

        mainViewModel.getLiveStreamPlaying().observe(this, isPlaying -> {
            L.i("LSF observed " + isPlaying);
            if (isPlaying == null) {
                toggle.setSelected(false);
                return;
            }

            toggle.setSelected(isPlaying);
        });

        return toggle;
    }
}
