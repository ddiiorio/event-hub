package com.dannydiiorio.eventhub;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dannydiiorio.eventhub.Adapter.EventAdapter;
import com.dannydiiorio.eventhub.Model.Event;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.dannydiiorio.eventhub.App.Constants.eventsAll;
import static com.dannydiiorio.eventhub.App.Constants.eventsUser;

public class EventViewActivity extends AppCompatActivity {
    private static final String TAG = "LOGTAG";
    protected RecyclerView recyclerView;
    private RecyclerView.Adapter myAdapter;
    protected DatabaseReference dbEvents;
    protected DatabaseReference dbUserEvents;
    private EditText newEventTitle, newEventThumb;
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSwipeRefreshLayout = findViewById(R.id.content);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, SignInActivity.class);
            finish();
            startActivity(intent);
            Log.w(TAG, "No User");
        } else {
            Log.w(TAG, "User exists");
            App.Constants.profileImage = Uri.parse(FirebaseAuth.getInstance()
                    .getCurrentUser().getPhotoUrl().toString());
            App.Constants.currentUser = FirebaseAuth.getInstance().getCurrentUser();


            App.Constants.vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            CircleImageView profile = findViewById(R.id.profileImage);
            Picasso.get()
                    .load(App.Constants.profileImage)
                    .placeholder(R.drawable.empty_profile)
                    .into(profile);

            // This method performs the actual data-refresh operation.
// The method calls setRefreshing(false) when it's finished.
            mSwipeRefreshLayout.setOnRefreshListener(
                    this::setupFirebaseEvents
            );

            setupFirebaseEvents();
            setupFab();

            recyclerView = findViewById(R.id.recycler_view);
            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.addItemDecoration(new GridSpacingItemDecoration(2,
                    dpToPx(10), true));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }
    }

    private void setupFirebaseEvents() {
        mSwipeRefreshLayout.setRefreshing(true);
        dbEvents = App.Constants.database.child("events");
        Query dataQuery = dbEvents.orderByChild("eventDateMillis");
        dbUserEvents = App.Constants.database.child("users/")
                .child(App.Constants.currentUser.getUid()).child("events");

        dataQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    Event event = dataSnapshot1.getValue(Event.class);
                    eventsAll.put(event.getId(), event);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        dbUserEvents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventsUser = new ArrayList<>();
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()) {
                    eventsUser.add(eventsAll.get(dataSnapshot1.getKey()));
                }
                eventsUser.sort(new App.Constants.EventComparator());
                mSwipeRefreshLayout.setRefreshing(false);
                myAdapter = new EventAdapter(EventViewActivity.this, eventsUser);
                recyclerView.setAdapter(myAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    protected void setupFab() {
        final FloatingActionMenu fabMenu = findViewById(R.id.floatingMenu);
        FloatingActionButton fabQuickAdd = findViewById(R.id.fabQuickAdd);
        fabQuickAdd.setOnClickListener(view -> {
            fabMenu.close(true);
            addEventDialog();
        });
        FloatingActionButton fabTicketmaster = findViewById(R.id.fabAddTicketmaster);
        fabTicketmaster.setOnClickListener(v -> {
            fabMenu.close(true);
            Intent i = new Intent(v.getContext(), EventSearchActivity.class);
            v.getContext().startActivity(i);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.signOut:
                logout();
                return true;
            case R.id.menu_refresh:
                // Start the refresh background task.
                // This method calls setRefreshing(false) when it's finished.
                setupFirebaseEvents();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    private void addEventDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setTitle(R.string.addEventTitle);
        dialog.setContentView(R.layout.add_task_dialog);
        TextView newEventBtn = dialog.findViewById(R.id.newEventBtn);
        newEventTitle = dialog.findViewById(R.id.newEventTitle);
        newEventThumb = dialog.findViewById(R.id.newEventThumb);
        newEventTitle.requestFocus();

        newEventBtn.setOnClickListener(v -> {
            String errorMsg = getResources().getString(R.string.newEventError);
            String title = newEventTitle.getText().toString();
            String url = newEventThumb.getText().toString();

            if (title.length() > 0) {
                Event event = new Event();
                Date date = new Date();
                event.setTitle(title);
                event.setEventDate(App.Constants.df.format(date));
                event.setImgUrl(url);
                event.setEventDateMillis(date.getTime());
                event.setCustomEvent(true);
                event.setId(String.valueOf(event.hashCode()));
                newEventTitle.setText("");
                newEventThumb.setText("");
                App.Constants.eventsAll.put(event.getId(), event);
                eventsUser.add(event);
                dbEvents.child(event.getId()).setValue(event);
                dbUserEvents.child(event.getId()).setValue(true);
                dialog.dismiss();
                String eventAddConfirm = getResources().getString(R.string.eventAddConfirm);
                Snackbar.make(findViewById(android.R.id.content),
                        eventAddConfirm, Snackbar.LENGTH_LONG).show();
            } else {
                Toast.makeText(EventViewActivity.this,
                        errorMsg, Toast.LENGTH_LONG).show();
            }
        });
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(GridLayoutManager.LayoutParams.MATCH_PARENT,
                GridLayoutManager.LayoutParams.WRAP_CONTENT);
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, r.getDisplayMetrics()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        myAdapter.notifyDataSetChanged();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, SignInActivity.class);
        finish();
        startActivity(intent);
    }
}
