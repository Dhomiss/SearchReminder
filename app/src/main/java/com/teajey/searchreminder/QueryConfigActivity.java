package com.teajey.searchreminder;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class QueryConfigActivity extends AppCompatActivity {

    public static final int RESULT_DELETE = 2;
    
    private EditText queryEditor;
    private Spinner engineEditor;

    private SearchQuery searchQueryToEdit;
    private int queryIndex;
    private Context context;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_config);

        context = this;

        queryEditor = findViewById(R.id.edit_query_text);
        engineEditor = findViewById(R.id.edit_engine_spinner);
        Button deleteButton = findViewById(R.id.delete_button);
        Button confirmButton = findViewById(R.id.confirm_button);
        Button cancelButton = findViewById(R.id.cancel_button);

        Intent intent = getIntent();
        searchQueryToEdit = (SearchQuery) intent.getSerializableExtra(QueueRecyclerViewAdapter.SEARCH_QUERY);
        queryIndex = intent.getIntExtra(QueueRecyclerViewAdapter.QUERY_INDEX, 0);

        queryEditor.setText(searchQueryToEdit.getQuery());

        String searchEngines[] = getResources().getStringArray(R.array.search_engines);
        int searchEngineToSelect = 0;
        for (int i = 0; i < searchEngines.length; i++) {
            if (searchQueryToEdit.getSearchEngine().equals(searchEngines[i])) {
                searchEngineToSelect = i;
            }
        }
        engineEditor.setSelection(searchEngineToSelect);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                searchQueryToEdit.setQuery(queryEditor.getText().toString());
                searchQueryToEdit.setSearchEngine(engineEditor.getSelectedItem().toString());

                Intent returnIntent = new Intent();
                returnIntent.putExtra(QueueRecyclerViewAdapter.SEARCH_QUERY, searchQueryToEdit);
                returnIntent.putExtra(QueueRecyclerViewAdapter.QUERY_INDEX, queryIndex);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean askToDelete = preferences.getBoolean("confirm_delete", true);
                if (askToDelete) {
                    new AlertDialog.Builder(context)
                            .setTitle("Confirm")
                            .setMessage("Are you sure you want to delete this query?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra(QueueRecyclerViewAdapter.QUERY_INDEX, queryIndex);
                                    setResult(RESULT_DELETE, returnIntent);
                                    finish();
                                }
                            })
                            .setNegativeButton("No", null)
                            .create().show();
                } else {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(QueueRecyclerViewAdapter.QUERY_INDEX, queryIndex);
                    setResult(RESULT_DELETE, returnIntent);
                    finish();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
    }
}
