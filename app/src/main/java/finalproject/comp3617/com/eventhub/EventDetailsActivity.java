package finalproject.comp3617.com.eventhub;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.database.DatabaseReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import finalproject.comp3617.com.eventhub.Model.Event;

public class EventDetailsActivity extends AppCompatActivity {
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final String TAG = "LOGTAG";
    private static final double LAT = 49.316054;
    private static final double LON = -123.026416;
    private TextView eventDate, eventVenue, venueAddress, eventTitle;
    private String id = null, imageUrl;
    protected GeoDataClient mGeoDataClient;
    protected DatabaseReference db;
    protected Event current;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        mGeoDataClient = Places.getGeoDataClient(this, null);

        ImageView eventImage = findViewById(R.id.eventImage);
        eventTitle = findViewById(R.id.eventTitle);
        eventDate = findViewById(R.id.dateTxt);
        eventVenue = findViewById(R.id.venueTxt);
        venueAddress = findViewById(R.id.venueAddress);
        ImageView backBtn = findViewById(R.id.backBtn);
        TextView openMapsText = findViewById(R.id.openMapsText);
        db = App.Constants.database.child("events");

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getStringExtra("id") != null) {
                id = intent.getStringExtra("id");
                eventTitle.setText(intent.getStringExtra("title"));
                String dateTemp = intent.getStringExtra("date");
                if (dateTemp != null) {
                    eventDate.setText(dateTemp);
                }
                imageUrl = intent.getStringExtra("image");
                ImageHelper.loadImage(imageUrl, eventImage);
                String placeId = intent.getStringExtra("placeId");
                if (placeId != null) {
                    mGeoDataClient.getPlaceById(placeId)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    PlaceBufferResponse places = task.getResult();
                                    Place myPlace = places.get(0);
                                    eventVenue.setText(myPlace.getName());
                                    venueAddress.setText(myPlace.getAddress());
                                    Log.i(TAG, "Place found: " + myPlace.getName());
                                    places.release();
                                } else {
                                    Log.e(TAG, "Place not found.");
                                }
                            });
                }
                if (intent.getStringExtra("venueName") != null) {
                    eventVenue.setText(intent.getStringExtra("venueName"));
                }
                if (intent.getStringExtra("venueAddress") != null) {
                    venueAddress.setText(intent.getStringExtra("venueAddress"));
                }
            }
        }

        eventVenue.setOnClickListener(v -> {
            LatLng neCorner = new LatLng(LAT, LON); //49.316054, -123.026416
            LatLng swCorner = new LatLng(LAT-0.0976, LON-0.1888);
            if (ActivityCompat.checkSelfPermission(EventDetailsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(EventDetailsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PLACE_PICKER_REQUEST);
                return;
            }
            try {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                builder.setLatLngBounds(new LatLngBounds(swCorner, neCorner));
                Intent i = builder.build(EventDetailsActivity.this);
                startActivityForResult(i, PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                Log.e(TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
            } catch (Exception e) {
                Log.e(TAG, String.format("PlacePicker Exception: %s", e.getMessage()));
            }
        });

        backBtn.setOnClickListener(v -> finish());
        openMapsText.setOnClickListener(v -> launchGoogleMaps());
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
        db.child(id).child("eventDate").setValue(dateString);
        db.child(id).child("eventDateMillis").setValue(d.getTime());
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                final Place place = PlacePicker.getPlace(data, this);
                eventVenue.setText(place.getName());
                venueAddress.setText(place.getAddress());
//                realmDb.executeTransaction(new Realm.Transaction() {
//                    @Override
//                    public void execute(Realm realm) {
//                        Event temp = realm.where(Event.class).equalTo("id", id)
//                                .findFirstAsync();
//                        if (temp != null) {
//                            temp.setPlaceId(place.getId());
//                            temp.setImgUrl(imageUrl);
//                            temp.setTitle(eventTitle.getText().toString());
//                            temp.setEventDate(parseDate(eventDate.getText().toString()));
//                        }
//                        realmDb.copyToRealmOrUpdate(temp);
//                    }
//                });
                String venueConfirm = getResources().getString(R.string.venueConfirm);
                Snackbar.make(findViewById(android.R.id.content),
                        venueConfirm, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    public void showDatePickerDialog(View v) {
        DatePickerFragment newFrag = new DatePickerFragment();
        newFrag.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("EEE, d MMM yyyy").parse(date);
        } catch (ParseException e) {
            return null;
        }
    }
}
