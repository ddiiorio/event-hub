package com.dannydiiorio.eventhub.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.VibrationEffect;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dannydiiorio.eventhub.App;
import com.dannydiiorio.eventhub.EventDetailsActivity;
import com.dannydiiorio.eventhub.EventViewActivity;
import com.dannydiiorio.eventhub.ImageHelper;
import com.dannydiiorio.eventhub.Model.Event;
import com.dannydiiorio.eventhub.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.dannydiiorio.eventhub.App.Constants.vibe;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private ArrayList<Event> events;
    private Context context;

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView thumbImg;
        ImageView deleteOption;
        TextView countdown;
        TextView eventDate;
        CardView cardView;

        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            thumbImg = v.findViewById(R.id.thumbImg);
            deleteOption = v.findViewById(R.id.deleteOption);
            eventDate = v.findViewById(R.id.date);
            countdown = v.findViewById(R.id.countdown);
            cardView = v.findViewById(R.id.card_view);
        }
    }

    public EventAdapter(Context context, ArrayList<Event> events) {
        super();
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_card, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Event event = events.get(position);
        View.OnClickListener onClick;
        View.OnClickListener onClickDelete;
        holder.title.setText(event.getTitle());
        holder.eventDate.setText(event.getEventDate());
        String imageUrl = event.getImgUrl();
        ImageHelper.loadThumb(imageUrl, holder.thumbImg);
        Date endDate = App.Constants.parseFirebaseDate(event.getEventDate());
        long diff = endDate.getTime() - System.currentTimeMillis();
        double days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        int x = (int) Math.round(days);
        if (x == 0) {
            holder.countdown.setTextColor(Color.RED);
            holder.countdown.setText(String.valueOf("Starts: TODAY!!"));
        } else if (isNegative(days)) {
            holder.countdown.setText(String.valueOf("Passed"));
        } else {
            String dayDiff = "Starts: " + (x + 1) + " days";
            holder.countdown.setText(dayDiff);
        }

        onClick = v -> {
            Intent viewEvent = new Intent(v.getContext(), EventDetailsActivity.class);
            viewEvent.putExtra("id", events.get(position).getId());
            viewEvent.putExtra("placeId", events.get(position).getPlaceId());
            viewEvent.putExtra("venueName", events.get(position).getVenueName());
            viewEvent.putExtra("venueAddress", events.get(position).getVenueAddress());
            v.getContext().startActivity(viewEvent);
        };
        holder.cardView.setOnClickListener(onClick);
        holder.thumbImg.setOnClickListener(onClick);

        onClickDelete = v -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(context, holder.deleteOption);
            //inflating menu from xml resource
            popup.inflate(R.menu.menu_event);
            //adding click listener
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.removeEvent:
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

                        //not removing items if cancel is done
                        builder.setPositiveButton((R.string.remove),
                                (dialog, which) -> {
                                    //code to delete event
                                    App.Constants.removeEvent(events.get(position).getId());
                                    dialog.dismiss();
                                    Snackbar.make(((EventViewActivity)context).findViewById(R.id.eventContent),
                                            R.string.removed, Snackbar.LENGTH_LONG).show();
                                }).setNegativeButton((android.R.string.cancel),
                                (dialog, which) -> dialog.dismiss()).show();
                        return true;
                    default:
                        return false;
                }
            });
            //displaying the popup
            popup.show();
        };

        holder.deleteOption.setOnClickListener(onClickDelete);
        holder.countdown.setOnClickListener(onClickDelete);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    private static boolean isNegative(double d) {
        return Double.compare(d, 0.0) < 0;
    }
}
