package com.test.chat.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.test.chat.R;

import org.json.JSONObject;

public class AppListDetailsFragment extends Fragment {

    private static final String PARAM = "param";

    private String param;

    public AppListDetailsFragment() {
    }

    public static AppListDetailsFragment newInstance(JSONObject jsonObject) {
        AppListDetailsFragment fragment = new AppListDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PARAM, jsonObject.toString());
        fragment.setArguments(bundle);
        return fragment;
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
        View view = inflater.inflate(R.layout.fragment_app_list_details, container, false);
        TextView textView = view.findViewById(R.id.textView);
        textView.setText(param);
        return view;
    }
}