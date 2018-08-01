package finalproject.comp3617.com.eventhub;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import finalproject.comp3617.com.eventhub.Realm.Event;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public class EventAdapter extends RealmRecyclerViewAdapter<Event, EventAdapter.ViewHolder> {
    private RealmResults<Event> events;

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView thumbImg;
        TextView countdown;
        TextView eventDate;
        CardView cardView;

        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            thumbImg = v.findViewById(R.id.thumbImg);
            eventDate = v.findViewById(R.id.date);
            countdown = v.findViewById(R.id.countdown);
            cardView = v.findViewById(R.id.card_view);
        }
    }

    EventAdapter(@Nullable OrderedRealmCollection<Event> data,
                 boolean autoUpdate, RealmResults<Event> events) {
        super(data, autoUpdate);
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
        holder.title.setText(event.getTitle());
        holder.eventDate.setText(new SimpleDateFormat("MMMM d yyyy")
                .format(event.getEventDate()));
        String imageUrl = event.getImgUrl();
        ImageHelper.loadThumb(imageUrl, holder.thumbImg);
        Date endDate = event.getEventDate();
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

        onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewEvent = new Intent(v.getContext(), EventDetailsActivity.class);
                viewEvent.putExtra("id", events.get(position).getId());
                viewEvent.putExtra("title", holder.title.getText().toString());
                viewEvent.putExtra("date", new SimpleDateFormat("EEE, d MMM yyyy",
                        Locale.getDefault())
                        .format(events.get(position).getEventDate()));
                viewEvent.putExtra("image", events.get(position).getImgUrl());
                viewEvent.putExtra("placeId", events.get(position).getPlaceId());
                viewEvent.putExtra("venueName", events.get(position).getVenueName());
                viewEvent.putExtra("venueAddress", events.get(position).getVenueAddress());
                v.getContext().startActivity(viewEvent);
            }
        };
        holder.cardView.setOnClickListener(onClick);
        holder.thumbImg.setOnClickListener(onClick);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    private static boolean isNegative(double d) {
        return Double.compare(d, 0.0) < 0;
    }
}
