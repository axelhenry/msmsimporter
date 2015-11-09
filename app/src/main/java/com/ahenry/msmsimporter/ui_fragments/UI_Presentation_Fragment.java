package com.ahenry.msmsimporter.ui_fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ahenry.msmsimporter.R;

//import android.app.Fragment;

/**
 * Created by axel on 10/06/15.
 */
public class UI_Presentation_Fragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.ui_presentation_fragment, container, false);
    }

/*    public void initFragment(JustifiedTextView.TextLinkClickListener aListener, int aColor){
        TextView tv = (TextView) getView().findViewById(R.id.noticeText);
        //JustifiedTextView tv = (JustifiedTextView) getView().findViewById(R.id.noticeText);

        //tv.setOnTextLinkClickListener(aListener);
        //tv.setLinksClickable(true);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setLinkTextColor(aColor);

    }*/

}


