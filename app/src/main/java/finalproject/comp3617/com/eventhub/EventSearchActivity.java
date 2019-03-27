package finalproject.comp3617.com.eventhub;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.ticketmaster.discovery.model.Venue;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static finalproject.comp3617.com.eventhub.App.Constants.eventsUser;

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
    private ProgressDialog progDialog;
    private LinearLayout result0, result1, result2;
    protected DatabaseReference db;

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
            if (progDialog != null && progDialog.isShowing()) {
                progDialog.dismiss();
                progDialog = null;
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
//        builder.setIcon(R.drawable.ic_add_box_black_24dp);

        builder.setPositiveButton(getText(R.string.confirm), (dialog, which) -> {
            //code to add event
            finalproject.comp3617.com.eventhub.Model.Event event
                    = new finalproject.comp3617.com.eventhub.Model.Event();
            event.setTitle(e.getName());
            event.setCustomEvent(false);
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
            int eventHash = event.hashCode();
            event.setId(String.valueOf(eventHash));
            if (App.Constants.eventsAll.containsKey(event.getId())) {
                if (!eventsUser.contains(event)) {
                    eventsUser.add(event);
                    db.child("users/").child(App.Constants.currentUser.getUid()).child("events")
                            .child(event.getId()).setValue(true);
                    dialog.dismiss();
                    finish();
                } else {
                    dialog.dismiss();
                    String alreadyExists = getResources().getString(R.string.alreadyExists);
                    Snackbar.make(findViewById(android.R.id.content),
                            alreadyExists, Snackbar.LENGTH_LONG).show();
                }
            } else {
                App.Constants.eventsAll.put(event.getId(), event);
                eventsUser.add(event);
                db.child("events").child(event.getId()).setValue(event);
                db.child("users/").child(App.Constants.currentUser.getUid()).child("events")
                        .child(event.getId()).setValue(true);
                dialog.dismiss();
                finish();
            }
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
