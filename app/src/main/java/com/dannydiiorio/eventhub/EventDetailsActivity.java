package com.dannydiiorio.eventhub;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dannydiiorio.eventhub.Model.Event;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.dannydiiorio.eventhub.App.Constants.eventsAll;
import static com.dannydiiorio.eventhub.App.Constants.eventsUser;
import static com.dannydiiorio.eventhub.App.Constants.vibe;

public class EventDetailsActivity extends AppCompatActivity {
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final String TAG = "LOGTAG";
    private static double LAT = 49.316054;
    private static double LON = -123.026416;
    private TextView eventVenue;
    private TextView venueAddress;
    private TextView eventTitle;
    private TextView eventDate;
    private TextView openMapsText;
    private TextView ticketUrlText;
    private ImageView eventImage, tmLogo;
    private Button deleteBtn;
    private String id = null;
    private String ticketLink = null;
    private boolean isCustom;
    private FusedLocationProviderClient fusedLocationClient;
    protected DatabaseReference db;
    protected Event current;
    protected int currentIndex;
    private LinearLayout ticketUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        eventImage = findViewById(R.id.eventImage);
        tmLogo = findViewById(R.id.ticketmasterLogo);
        eventTitle = findViewById(R.id.eventTitle);
        eventDate = findViewById(R.id.dateTxt);
        eventVenue = findViewById(R.id.venueTxt);
        venueAddress = findViewById(R.id.venueAddress);
        openMapsText = findViewById(R.id.openMapsText);
        ticketUrlText = findViewById(R.id.openTicketUrl);
        ticketUrl = findViewById(R.id.ticketUrl);
        deleteBtn = findViewById(R.id.deleteBtn);

        db = App.Constants.database.child("events");

        getIntentData();
        setupListeners();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getStringExtra("id") != null) {
                id = intent.getStringExtra("id");
                current = App.Constants.eventsAll.get(id);
                isCustom = current.isCustomEvent();
                currentIndex = eventsUser.indexOf(current);
                eventTitle.setText(current.getTitle());
                eventDate.setText(current.getEventDate());
                ImageHelper.loadImage(current.getImgUrl(), eventImage);
                if (intent.getStringExtra("venueName") != null) {
                    eventVenue.setText(intent.getStringExtra("venueName"));
                }
                if (intent.getStringExtra("venueAddress") != null) {
                    venueAddress.setText(intent.getStringExtra("venueAddress"));
                }
                if (intent.getStringExtra("ticketUrl") != null) {
                    ticketLink = intent.getStringExtra("ticketUrl");
                } else {
                    ticketUrl.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * If event is custom, aka not from Ticketmaster, user can edit venue and date
     */
    private void setupListeners() {
        if (isCustom) {
            tmLogo.setVisibility(View.GONE);
            ticketUrl.setVisibility(View.GONE);
            Places.initialize(getApplicationContext(),
                    getResources().getString(R.string.google_api_key));

            eventVenue.setOnClickListener(v -> {
                if (ActivityCompat.checkSelfPermission(EventDetailsActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EventDetailsActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            AUTOCOMPLETE_REQUEST_CODE);
                    return;
                }
                try {
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(this, location -> {
                                // Get last known location
                                if (location != null) {
                                    LAT = location.getLatitude();
                                    LON = location.getLongitude();
                                }
                            });
                    //Retrieve place name, id, and address only
                    List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                            Place.Field.ADDRESS);
                    Intent intent = new Autocomplete.IntentBuilder(
                            AutocompleteActivityMode.FULLSCREEN, fields)
                            .setLocationBias(RectangularBounds.newInstance(
                                    new LatLng(LAT-0.08, LON-0.12),
                                    new LatLng(LAT+0.08, LON+0.12)))
                            .build(this);
                    startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
                } catch (Exception e) {
                    Log.e(TAG, String.format("Places Exception: %s", e.getMessage()));
                }
            });

            eventDate.setOnClickListener(this::showDatePickerDialog);
        }

        openMapsText.setOnClickListener(v -> launchGoogleMaps());

        if (ticketUrl.getVisibility() == View.VISIBLE) {
            ticketUrlText.setOnClickListener(v -> openTicketUrl());
        }

        deleteBtn.setOnClickListener((View v) -> {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(v.getContext());
            builder.setMessage(R.string.deleteDialog);
            builder.setTitle(R.string.deleteEventTitle);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibe.vibrate(VibrationEffect.createOneShot(50,
                        VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                vibe.vibrate(50);
            }

            builder.setPositiveButton((R.string.remove),
                    (dialog, which) -> {
                        //delete event
                        App.Constants.removeEvent(id);
                        dialog.dismiss();
                        finish();
                    }).setNegativeButton((android.R.string.cancel),
                    (dialog, which) -> dialog.dismiss()).show();
        });
    }

    private void launchGoogleMaps() {
        String venue = eventVenue.getText().toString();
        if (!venue.equals("Select a venue")) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + venue);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        } else {
            String mapsError = getResources().getString(R.string.mapsError);
            Snackbar.make(findViewById(android.R.id.content), mapsError, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    public void saveSelectedDate(Date d) {
        String dateString = App.Constants.df.format(d);
        current.setEventDate(dateString);
        current.setEventDateMillis(d.getTime());
        saveEvent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                eventVenue.setText(place.getName());
                venueAddress.setText(place.getAddress());
                current.setPlaceId(place.getId());
                current.setVenueAddress(String.valueOf(place.getAddress()));
                current.setVenueName(String.valueOf(place.getName()));
                saveEvent();
                String venueConfirm = getResources().getString(R.string.venueConfirm);
                Snackbar.make(findViewById(android.R.id.content),
                        venueConfirm, Snackbar.LENGTH_LONG).show();
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
                String venueErr = getResources().getString(R.string.venueError);
                Snackbar.make(findViewById(android.R.id.content),
                        venueErr, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    protected void saveEvent() {
        eventsUser.set(currentIndex, current);
        eventsAll.put(id, current);
        Map<String, Object> update = new HashMap<>();
        update.put(id, current);
        db.updateChildren(update);
    }

    public void showDatePickerDialog(View v) {
        DatePickerFragment newFrag = new DatePickerFragment();
        newFrag.show(getSupportFragmentManager(), "datePicker");
    }

    public void openTicketUrl() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(ticketLink));
        startActivity(i);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
