package com.teajey.searchreminder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class SearchFragment extends Fragment {

    private ImageView logoImageView;
    private Spinner searchEngineSpinner;

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void setSearchEngine(int i) {
        switch (i) {
            case 0: //Google
                logoImageView.setImageResource(R.drawable.google_logo);
                searchEngineSpinner.setSelection(i);
                break;
            case 1: //Wikipedia
                logoImageView.setImageResource(R.drawable.wikipedia_logo);
                searchEngineSpinner.setSelection(i);
                break;
            case 2: //Bing
                logoImageView.setImageResource(R.drawable.bing_logo);
                searchEngineSpinner.setSelection(i);
                break;
            case 3: //Yahoo!
                logoImageView.setImageResource(R.drawable.yahoo_logo);
                searchEngineSpinner.setSelection(i);
                break;
            case 4: //YouTube
                logoImageView.setImageResource(R.drawable.youtube_logo);
                searchEngineSpinner.setSelection(i);
                break;
            case 5: //Duck Duck Go
                logoImageView.setImageResource(R.drawable.duck_duck_go_logo);
                searchEngineSpinner.setSelection(i);
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        logoImageView = view.findViewById(R.id.logoImage);
        EditText queryTextBox = view.findViewById(R.id.queryBox);
        searchEngineSpinner = view.findViewById(R.id.searchEngineSpinner);

        queryTextBox.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                        ((MainActivity)getContext()).addSearchQuery();
                        return true;
                    }
                }
        );

        String searchEnginePreference = PreferenceManager.getDefaultSharedPreferences(this.getContext()).getString("default_search_engine", "");
        String searchEngineNames[] = getResources().getStringArray(R.array.search_engines);
        int searchEngineCode = 0;
        for (int i = 0; i < searchEngineNames.length; i++) {
            if (searchEngineNames[i].equals(searchEnginePreference)) {
                searchEngineCode = i;
                break;
            }
        }
        this.setSearchEngine(searchEngineCode);
        final SearchFragment thisFragment = this;
        searchEngineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                thisFragment.setSearchEngine(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                thisFragment.setSearchEngine(0);
            }
        });

        return view;
    }

    interface OnFragmentInteractionListener {}
}
