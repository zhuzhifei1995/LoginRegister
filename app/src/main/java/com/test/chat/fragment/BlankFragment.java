package com.test.chat.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.test.chat.R;
import com.test.chat.util.ActivityUtil;

@RequiresApi(api = Build.VERSION_CODES.M)
public class BlankFragment extends Fragment {

    private static final String TAG = ActivityUtil.TAG;
    private static final String PARAM = "param";
    private String param;

    public BlankFragment() {
    }

    public static BlankFragment newInstance(String param) {
        BlankFragment blankFragment = new BlankFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PARAM, param);
        blankFragment.setArguments(bundle);
        return blankFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            param = getArguments().getString(PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blank, container, false);
        TextView hello_blank_fragment = view.findViewById(R.id.hello_blank_fragment);
        hello_blank_fragment.setText(param);
        return view;
    }

}