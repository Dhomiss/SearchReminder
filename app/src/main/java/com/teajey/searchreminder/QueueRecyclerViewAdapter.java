package com.teajey.searchreminder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class QueueRecyclerViewAdapter extends RecyclerView.Adapter<QueueRecyclerViewAdapter.ViewHolder> {

    public static final String SEARCH_QUERY = "com.teajey.searchreminder.SEARCH_QUERY";
    public static final String QUERY_INDEX = "com.teajey.searchreminder.QUERY_INDEX";
    
    private Context context;
    private ArrayList<SearchQuery> searchQueries;

    QueueRecyclerViewAdapter(Context c) {
        this.context = c;
        this.searchQueries = ((MainActivity)c).getSearchQueryQueue();
    }

    @Override
    public QueueRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.query_queue_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(QueueRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.queryText.setText(searchQueries.get(position).getQuery());
        holder.engineTitle.setText(searchQueries.get(position).getSearchEngine());

        final int index = position;
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openQueryConfigFor(searchQueries.get(index), index);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.searchQueries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView queryText;
        private TextView engineTitle;
        private Button editButton;

        public ViewHolder(View itemView) {
            super(itemView);
            queryText = itemView.findViewById(R.id.queryText);
            engineTitle = itemView.findViewById(R.id.engineTitle);
            editButton = itemView.findViewById(R.id.queueItemConfigButton);
        }
    }

    private void openQueryConfigFor(SearchQuery sq, int index) {
        Intent intent = new Intent(context, QueryConfigActivity.class);
        intent.putExtra(SEARCH_QUERY, sq);
        intent.putExtra(QUERY_INDEX, index);
        ((Activity)context).startActivityForResult(intent, 1);
    }
}
