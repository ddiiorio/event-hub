package finalproject.comp3617.com.eventhub;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ticketmaster.api.discovery.DiscoveryApi;
import com.ticketmaster.api.discovery.operation.SearchEventsOperation;
import com.ticketmaster.api.discovery.response.PagedResponse;
import com.ticketmaster.discovery.model.Event;
import com.ticketmaster.discovery.model.Events;
import com.ticketmaster.discovery.model.Venue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventSearchActivity extends AppCompatActivity {
    private static final String tmApiKey = "WMqw4xi5StCjkwj6c1ifQnxlmVuBGxDw";
    private EditText citySearch;
    private TextView resultTitle0, resultVenue0, resultDate0;
    private TextView resultTitle1, resultVenue1, resultDate1;
    private TextView resultTitle2, resultVenue2, resultDate2;
    private View line0, line1;
    private String city, keyword;
    private DiscoveryApi discoveryApi;
    private Event event0, event1, event2;
    private ProgressDialog progDailog;
    private LinearLayout result0, result1, result2;
    protected DatabaseReference db;
    protected Map<String, finalproject.comp3617.com.eventhub.Model.Event> events1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_search);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setupFirebase();

        discoveryApi = new DiscoveryApi(tmApiKey);
        citySearch = findViewById(R.id.citySearch);
        final SearchView keywordSearch = findViewById(R.id.keywordSearch);
        resultTitle0 = findViewById(R.id.resultTitle0);
        resultVenue0 = findViewById(R.id.resultVenue0);
        resultDate0 = findViewById(R.id.resultDate0);
        line0 = findViewById(R.id.linebreak0);
        resultTitle1 = findViewById(R.id.resultTitle1);
        resultVenue1 = findViewById(R.id.resultVenue1);
        resultDate1 = findViewById(R.id.resultDate1);
        line1 = findViewById(R.id.linebreak1);
        resultTitle2 = findViewById(R.id.resultTitle2);
        resultVenue2 = findViewById(R.id.resultVenue2);
        resultDate2 = findViewById(R.id.resultDate2);
        result0 = findViewById(R.id.searchResult0);
        result1 = findViewById(R.id.searchResult1);
        result2 = findViewById(R.id.searchResult2);
        result0.setVisibility(LinearLayout.GONE);
        line0.setVisibility(View.INVISIBLE);
        result1.setVisibility(LinearLayout.GONE);
        line1.setVisibility(View.INVISIBLE);
        result2.setVisibility(LinearLayout.GONE);
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
        result0.setOnClickListener(v -> {
            if (event0 != null) {
                addEventDialog(event0);
            }
        });
        result1.setOnClickListener(v -> {
            if (event0 != null) {
                addEventDialog(event1);
            }
        });
        result2.setOnClickListener(v -> {
            if (event0 != null) {
                addEventDialog(event2);
            }
        });
    }

    private void setupFirebase() {
        db = App.Constants.database.child("events");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                events1 = new HashMap<>();
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                {
                    finalproject.comp3617.com.eventhub.Model.Event event
                            = dataSnapshot1.getValue(finalproject.comp3617.com.eventhub.Model.Event.class);
                    events1.put(event.getId(), event);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EventSearchActivity.this, "Oops.... Something is wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class Ticketmaster extends AsyncTask<Void, Void, List<Event>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog = new ProgressDialog(EventSearchActivity.this);
            if (!isFinishing()) {
                progDailog.setMessage("Searching...");
                progDailog.setIndeterminate(false);
                progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progDailog.setCancelable(true);
                try {
                    progDailog.show();
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
                        .pageSize(3)
                        .sort("date,asc")
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<Event> tmEvents = (page != null && page.getContent() != null)
                    ? page.getContent().getEvents() : Collections.<Event>emptyList();
            return tmEvents;
        }

        @Override
        protected void onPostExecute(List<Event> events) {
            if (progDailog != null && progDailog.isShowing()) {
                progDailog.dismiss();
                progDailog = null;
            }
            if (!events.isEmpty()) {
                result0.setVisibility(LinearLayout.VISIBLE);
                event0 = events.get(0);
                resultTitle0.setTextColor(ContextCompat.getColor(getApplicationContext(),
                        R.color.colorPrimaryDark));
                resultTitle0.setText(event0.getName());
                List<Venue> venues0 = event0.getVenues();
                resultVenue0.setText(venues0.get(0).getName());
                java.util.Date date0 = App.Constants.parseDate(
                        event0.getDates().getStart().getLocalDate());
                resultDate0.setText(App.Constants.df.format(date0));
                if (events.size() > 1 && events.get(1) != null) {
                    result1.setVisibility(LinearLayout.VISIBLE);
                    line0.setVisibility(View.VISIBLE);
                    event1 = events.get(1);
                    resultTitle1.setTextColor(ContextCompat.getColor(getApplicationContext(),
                            R.color.colorPrimaryDark));
                    resultTitle1.setText(event1.getName());
                    List<Venue> venues1 = event1.getVenues();
                    resultVenue1.setText(venues1.get(0).getName());
                    java.util.Date date1 = App.Constants.parseDate(
                            event1.getDates().getStart().getLocalDate());
                    resultDate1.setText(App.Constants.df.format(date1));
                    if (events.size() > 2 && events.get(2) != null) {
                        result2.setVisibility(LinearLayout.VISIBLE);
                        line1.setVisibility(View.VISIBLE);
                        event2 = events.get(2);
                        resultTitle2.setTextColor(ContextCompat.getColor(getApplicationContext(),
                                R.color.colorPrimaryDark));
                        resultTitle2.setText(event2.getName());
                        List<Venue> venues2 = event2.getVenues();
                        resultVenue2.setText(venues2.get(0).getName());
                        java.util.Date date2 = App.Constants.parseDate(
                                event2.getDates().getStart().getLocalDate());
                        resultDate2.setText(App.Constants.df.format(date2));
                    }
                }
            } else {
                String noResult = getResources().getString(R.string.noResultMsg);
                result0.setVisibility(LinearLayout.VISIBLE);
                resultTitle0.setText(noResult);
                resultVenue0.setText("");
                resultDate0.setText("");
                result1.setVisibility(LinearLayout.GONE);
                line0.setVisibility(View.INVISIBLE);
                result2.setVisibility(LinearLayout.GONE);
                line1.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void addEventDialog(final Event e) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(EventSearchActivity.this);
        builder.setMessage(getText(R.string.addEventDialog));
        builder.setTitle(R.string.addEventTitle);
        builder.setIcon(R.drawable.ic_add_box_black_24dp);

        builder.setPositiveButton(getText(R.string.confirm), (dialog, which) -> {
            //code to add event
            finalproject.comp3617.com.eventhub.Model.Event event
                    = new finalproject.comp3617.com.eventhub.Model.Event();
            event.setId(UUID.randomUUID().toString());
            event.setTitle(e.getName());
            java.util.Date eventDate = App.Constants
                    .parseDate(e.getDates().getStart().getLocalDate());
            event.setEventDate(App.Constants.df.format(eventDate));
            event.setImgUrl(e.getImages().get(0).getUrl());
            event.setVenueName(e.getVenues().get(0).getName());
            event.setEventDateMillis(eventDate.getTime());
            String addy1 = e.getVenues().get(0).getAddress().getLine1();
            String addy2 = e.getVenues().get(0).getAddress().getLine2();
            if (addy2 != null) {
                event.setVenueAddress(addy1 + addy2);
            } else {
                event.setVenueAddress(addy1);
            }
            events1.put(event.getId(), event);
            db.setValue(events1);
            dialog.dismiss();
            finish();
        }).setNegativeButton(getText(android.R.string.cancel),
                (dialog, which) -> dialog.dismiss()).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(progDailog != null) {
            progDailog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(progDailog != null) {
            progDailog.dismiss();
        }
    }
}
