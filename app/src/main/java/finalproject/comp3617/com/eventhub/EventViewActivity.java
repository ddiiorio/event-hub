package finalproject.comp3617.com.eventhub;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import finalproject.comp3617.com.eventhub.Model.Event;

public class EventViewActivity extends AppCompatActivity {
    private static final String TAG = "LOGTAG";
    private static final String FLAG = "FLAG";
    protected RecyclerView recyclerView;
    private RecyclerView.Adapter myAdapter;
    private ArrayList<Event> events;
    protected DatabaseReference db;
    private EditText newEventTitle, newEventThumb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String fromSignIn = intent.getStringExtra("signIn");
        String swipeMessage = getResources().getString(R.string.swipeMessage);
        final Snackbar snackBar = Snackbar.make(findViewById(android.R.id.content),
                swipeMessage, Snackbar.LENGTH_INDEFINITE);
        if (fromSignIn.equals(FLAG)) {
            snackBar.setAction("GOT IT!", v -> snackBar.dismiss());
            snackBar.show();
        }

        setupFab();

        db = App.Constants.database.child("events");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                events = new ArrayList<>();
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                {
                    Event event = dataSnapshot1.getValue(Event.class);
                    events.add(event);
                }
                myAdapter = new EventAdapter(EventViewActivity.this,events);
                recyclerView.setAdapter(myAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EventViewActivity.this, "Oops.... Something is wrong", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2,
                dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

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
        getMenuInflater().inflate(R.menu.menu_event_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.signOut:
//                SyncUser syncUser = SyncUser.current();
//                if (syncUser != null) {
//                    syncUser.logOut();
//                    Intent i = new Intent(this, SignInActivity.class);
//                    startActivity(i);
//                    finish();
//                }
                logout();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        refreshList();
//    }

//    protected void refreshList() {
//        myAdapter = new EventAdapter(realmDb.where(Event.class)
//                .sort("eventDate", Sort.ASCENDING)
//                .findAllAsync(),true,events);
//        recyclerView.setAdapter(myAdapter);
//    }

    private void addEventDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setTitle(R.string.addEventTitle);
        dialog.setContentView(R.layout.add_task_dialog);
        Button newEventBtn = dialog.findViewById(R.id.newEventBtn);
        newEventTitle = dialog.findViewById(R.id.newEventTitle);
        newEventThumb = dialog.findViewById(R.id.newEventThumb);
        newEventTitle.requestFocus();

        newEventBtn.setOnClickListener(v -> {
            String errorMsg = getResources().getString(R.string.newEventError);
            String title = newEventTitle.getText().toString();
            String url = newEventThumb.getText().toString();

            if (title.length() > 0) {
//                Event event = new Event();
//                event.setId(UUID.randomUUID().toString());
//                event.setTitle(title);
//                event.setEventDate(new Date());
//                event.setImgUrl(url);
//                realmDb.beginTransaction();
//                realmDb.copyToRealmOrUpdate(event);
//                realmDb.commitTransaction();
//                newEventTitle.setText("");
//                newEventThumb.setText("");
//                refreshList();
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

//    private RealmResults<Event> setUpRealm() {
//        SyncConfiguration configuration = SyncUser.current()
//                .createConfiguration(REALM_BASE_URL + "/eventhub")
//                .build();
//        realmDb = Realm.getInstance(configuration);
//
//        return realmDb
//                .where(Event.class)
//                .sort("eventDate", Sort.ASCENDING)
//                .findAllAsync();
//    }

//    private void setUpItemTouchHelper() {
//        ItemTouchHelper.SimpleCallback simpleCallback =
//                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
//            @Override
//            public boolean onMove(RecyclerView recyclerView,
//                                  RecyclerView.ViewHolder viewHolder,
//                                  RecyclerView.ViewHolder target) {
//                return false;
//            }
//
//            @Override
//            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
//                final int position = viewHolder.getAdapterPosition();
//
//                if (direction == ItemTouchHelper.LEFT) {
//                    AlertDialog.Builder builder =
//                            new AlertDialog.Builder(EventViewActivity.this);
//                    builder.setMessage(getText(R.string.deleteDialog));
//                    builder.setTitle(R.string.deleteEventTitle);
//                    builder.setIcon(R.drawable.ic_warning_black_24dp);
//
//                    //not removing items if cancel is done
//                    builder.setPositiveButton(getText(R.string.remove),
//                            (dialog, which) -> {
//                                //code to delete event
//                                Event remove = events.get(position);
//                                realmDb.beginTransaction();
//                                remove.deleteFromRealm();
//                                realmDb.commitTransaction();
//                                dialog.dismiss();
//                                refreshList();
//                            }).setNegativeButton(getText(android.R.string.cancel),
//                            (dialog, which) -> dialog.dismiss()).show();
//                }
//            }
//        };
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
//        itemTouchHelper.attachToRecyclerView(recyclerView);
//    }

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
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart");
        super.onRestart();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        App.Constants.mGoogleSignInClient.signOut();
        Intent intent = new Intent(this, SignInActivity.class);
        finish();
        startActivity(intent);
    }
}
