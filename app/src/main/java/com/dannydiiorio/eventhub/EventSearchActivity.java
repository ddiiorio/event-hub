package com.dannydiiorio.eventhub;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.dannydiiorio.eventhub.Adapter.EventSearchAdapter;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;
import com.ticketmaster.api.discovery.DiscoveryApi;
import com.ticketmaster.api.discovery.operation.SearchEventsOperation;
import com.ticketmaster.api.discovery.response.PagedResponse;
import com.ticketmaster.discovery.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EventSearchActivity extends AppCompatActivity {
    private static final String tmApiKey = "WMqw4xi5StCjkwj6c1ifQnxlmVuBGxDw";
    private static final String TAG = "LOGTAG";
    private EditText citySearch;
    private String city, keyword;
    private DiscoveryApi discoveryApi;
    private TextView noResults;
    private ProgressDialog progDialog;
    protected DatabaseReference db;
    protected RecyclerView recyclerView;
    protected List<Event> tmEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_search);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        CircleImageView profile = findViewById(R.id.profileImage);
        Picasso.get()
                .load(App.Constants.profileImage)
                .placeholder(R.drawable.empty_profile)
                .into(profile);

        db = App.Constants.database;
        tmEvents = new ArrayList<>();
        noResults = findViewById(R.id.noResultsMsg);
        recyclerView = findViewById(R.id.searchResultsRecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        discoveryApi = new DiscoveryApi(tmApiKey);
        citySearch = findViewById(R.id.citySearch);
        final SearchView keywordSearch = findViewById(R.id.keywordSearch);

        citySearch.requestFocus();
        keywordSearch.setSubmitButtonEnabled(true);
        keywordSearch.setIconified(false);
        keywordSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                keyword = query;
                city = (!citySearch.getText().toString().equals(""))
                        ? citySearch.getText().toString() : "Vancouver";
                new Ticketmaster().execute();
                keywordSearch.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });
    }

    private class Ticketmaster extends AsyncTask<Void, Void, List<Event>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDialog = new ProgressDialog(EventSearchActivity.this);
            if (!isFinishing()) {
                progDialog.setMessage("Searching...");
                progDialog.setIndeterminate(false);
                progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progDialog.setCancelable(true);
                try {
                    progDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected List<Event> doInBackground(Void... voids) {
            PagedResponse<Events> page = null;
            try {
                page = discoveryApi.searchEvents(new SearchEventsOperation()
                        .city(city)
                        .keyword(keyword)
                        .pageSize(20)
                        .sort("date,asc")
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
            tmEvents = (page != null && page.getContent() != null)
                    ? page.getContent().getEvents() : Collections.<Event>emptyList();
            return tmEvents;
        }

        @Override
        protected void onPostExecute(List<Event> events) {
            if (progDialog != null && progDialog.isShowing()) {
                progDialog.dismiss();
                progDialog = null;
            }
            if (!events.isEmpty()) {
                List<Event> results = new ArrayList<>();
                for (Event e : events) {
                    if (e.getClassifications().get(0).getSegment().getName().contains("Music")
                            && results.size() < 4) {
                        results.add(e);
                    }
                }
                if (!results.isEmpty()) {
                    if (recyclerView.getVisibility() != View.VISIBLE) {
                        recyclerView.setVisibility(View.VISIBLE);
                        noResults.setVisibility(LinearLayout.GONE);
                    }
                    RecyclerView.Adapter myAdapter =
                            new EventSearchAdapter(EventSearchActivity.this, results);
                    recyclerView.setAdapter(myAdapter);
                    myAdapter.notifyDataSetChanged();
                } else {
                    if (recyclerView.getVisibility() != View.INVISIBLE) {
                        recyclerView.setVisibility(View.GONE);
                        noResults.setVisibility(LinearLayout.VISIBLE);
                    }
                }
            } else {
                if (recyclerView.getVisibility() != View.INVISIBLE) {
                    recyclerView.setVisibility(View.GONE);
                    noResults.setVisibility(LinearLayout.VISIBLE);
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(progDialog != null) {
            progDialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(progDialog != null) {
            progDialog.dismiss();
        }
    }
}
