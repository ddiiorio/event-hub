package finalproject.comp3617.com.eventhub;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;
import com.ticketmaster.api.discovery.DiscoveryApi;
import com.ticketmaster.api.discovery.operation.SearchEventsOperation;
import com.ticketmaster.api.discovery.response.PagedResponse;
import com.ticketmaster.discovery.model.Event;
import com.ticketmaster.discovery.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import finalproject.comp3617.com.eventhub.Adapter.EventSearchAdapter;

public class EventSearchActivity extends AppCompatActivity {
    private static final String tmApiKey = "WMqw4xi5StCjkwj6c1ifQnxlmVuBGxDw";
    private EditText citySearch;
    private String city, keyword;
    private DiscoveryApi discoveryApi;
    private TextView noResults;
    private ProgressDialog progDialog;
    protected DatabaseReference db;
    protected RecyclerView recyclerView;
    private RecyclerView.Adapter myAdapter;
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
                        .pageSize(4)
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
                Log.i("LOGTAG", "events exist");
                if (recyclerView.getVisibility() != View.VISIBLE) {
                    recyclerView.setVisibility(View.VISIBLE);
                    noResults.setVisibility(LinearLayout.GONE);
                }
                myAdapter = new EventSearchAdapter(EventSearchActivity.this, tmEvents);
                recyclerView.setAdapter(myAdapter);
                myAdapter.notifyDataSetChanged();
            } else {
                Log.i("LOGTAG", "events don't exist");
                if (recyclerView.getVisibility() != View.INVISIBLE) {
                    recyclerView.setVisibility(View.GONE);
                    noResults.setVisibility(LinearLayout.VISIBLE);
                }
            }
        }
    }

//    private void addEventDialog(final Event e) {
//        AlertDialog.Builder builder =
//                new AlertDialog.Builder(EventSearchActivity.this);
//        builder.setMessage(getText(R.string.addEventDialog));
//        builder.setTitle(R.string.addEventTitle);
////        builder.setIcon(R.drawable.ic_add_box_black_24dp);
//
//        builder.setPositiveButton(getText(R.string.confirm), (dialog, which) -> {
//            //code to add event
//            finalproject.comp3617.com.eventhub.Model.Event event
//                    = new finalproject.comp3617.com.eventhub.Model.Event();
//            event.setTitle(e.getName());
//            event.setCustomEvent(false);
//            java.util.Date eventDate = App.Constants
//                    .parseDate(e.getDates().getStart().getLocalDate());
//            event.setEventDate(App.Constants.df.format(eventDate));
//            event.setImgUrl(e.getImages().get(0).getUrl());
//            event.setVenueName(e.getVenues().get(0).getName());
//            event.setEventDateMillis(eventDate.getTime());
//            String addy1 = e.getVenues().get(0).getAddress().getLine1();
//            String addy2 = e.getVenues().get(0).getAddress().getLine2();
//            if (addy2 != null) {
//                event.setVenueAddress(addy1 + addy2);
//            } else {
//                event.setVenueAddress(addy1);
//            }
//            int eventHash = event.hashCode();
//            event.setId(String.valueOf(eventHash));
//            if (App.Constants.eventsAll.containsKey(event.getId())) {
//                if (!eventsUser.contains(event)) {
//                    eventsUser.add(event);
//                    db.child("users/").child(App.Constants.currentUser.getUid()).child("events")
//                            .child(event.getId()).setValue(true);
//                    dialog.dismiss();
//                    finish();
//                } else {
//                    dialog.dismiss();
//                    String alreadyExists = R.string.alreadyExists;
//                    Snackbar.make(findViewById(android.R.id.content),
//                            alreadyExists, Snackbar.LENGTH_LONG).show();
//                }
//            } else {
//                App.Constants.eventsAll.put(event.getId(), event);
//                eventsUser.add(event);
//                db.child("events").child(event.getId()).setValue(event);
//                db.child("users/").child(App.Constants.currentUser.getUid()).child("events")
//                        .child(event.getId()).setValue(true);
//                dialog.dismiss();
//                finish();
//            }
//        }).setNegativeButton(getText(android.R.string.cancel),
//                (dialog, which) -> dialog.dismiss()).show();
//    }

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
