package com.luis.cortes.vehiclediagnostics;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by luis_cortes on 5/12/18.
 */

public class ChooseDeviceFragment extends Fragment {
    public ChooseDeviceFragment() {
        // Empty constructor
    }

    public static ChooseDeviceFragment newInstance() {
        ChooseDeviceFragment frag = new ChooseDeviceFragment();
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
