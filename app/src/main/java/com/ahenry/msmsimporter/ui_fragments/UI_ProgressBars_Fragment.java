package com.ahenry.msmsimporter.ui_fragments;

//import android.app.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ahenry.msmsimporter.R;

/**
 * Created by axel on 10/06/15.
 */
public class UI_ProgressBars_Fragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.ui_progressbars_fragment, container, false);
    }

}
