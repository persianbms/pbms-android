package org.persianbms.andromeda;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class LiveStreamFragment extends Fragment {

    private MaterialButton toggle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        toggle = (MaterialButton) inflater.inflate(R.layout.fragment_live_stream, container, false);
        toggle.setOnClickListener(view -> onToggleStream());

        return toggle;
    }

    private void onToggleStream() {
        L.i("toggle stream");
    }

}
