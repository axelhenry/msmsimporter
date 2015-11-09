package com.ahenry.msmsimporter.ui_fragments;

//import android.app.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ahenry.msmsimporter.R;
import com.daimajia.numberprogressbar.NumberProgressBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by axel on 12/06/15.
 */
public class UI_ProgressBar_Fragment extends Fragment {

    public static final Logger LOG = LoggerFactory.getLogger(UI_ProgressBar_Fragment.class);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.ui_progressbar_fragment, container, false);

        LOG.debug("onCreateView :: rootView created");
        Bundle b = getArguments();
        LOG.debug("onCreateView :: getArguments()");
        if(b != null) {
            String name = b.getString("name");
            LOG.debug("onCreateView :: name : {}.", name);
            initFragment(rootView, name);
        }else{
            LOG.debug("onCreateView :: bundle is null right now, move along.");
        }
        return rootView;

        //return inflater.inflate(R.layout.ui_progressbar_fragment, container, false);
    }

    public void initFragment(String aElement){
        setTask(getResources().getString(R.string.processedElements),aElement);
        setNumber(aElement,0);
    }

    public void initFragment(View v, String aElement){
        setTask(v, getResources().getString(R.string.processedElements),aElement);
        setNumber(v, aElement, 0);
    }

    public void setNumber(View v, String aElement, int aNumber){
        TextView tv = (TextView) v.findViewById(R.id.numbers);
        String text = String.format(getResources().getString(R.string.elementsNumber), aNumber, aElement);
        tv.setText(text);
        if(aNumber > 0){
            NumberProgressBar npb = (NumberProgressBar) v.findViewById(R.id.progressBar);
            npb.setMax(aNumber);
        }
    }

    public void setTask(View v, String aTask, String aElement){
        TextView tv = (TextView) v.findViewById(R.id.information);

        String text = String.format(getResources().getString(R.string.genericElements), aTask,aElement);
        tv.setText(text);
    }

    public void setNumber(String aElement, int aNumber){
        TextView tv = (TextView) getView().findViewById(R.id.numbers);
        String text = String.format(getResources().getString(R.string.elementsNumber), aNumber, aElement);
        tv.setText(text);
        if(aNumber > 0){
            NumberProgressBar npb = (NumberProgressBar) getView().findViewById(R.id.progressBar);
            npb.setMax(aNumber);
        }
    }

    public void setTask(String aTask, String aElement){
        TextView tv = (TextView) getView().findViewById(R.id.information);

        String text = String.format(getResources().getString(R.string.genericElements), aTask,aElement);
        tv.setText(text);
    }
}
