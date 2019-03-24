package finalproject.comp3617.com.eventhub;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import finalproject.comp3617.com.eventhub.Model.Event;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private ArrayList<Event> events;
    Context context;

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

    EventAdapter(Context context, ArrayList<Event> events) {
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
            viewEvent.putExtra("title", holder.title.getText().toString());
            viewEvent.putExtra("date", events.get(position).getEventDate());
            viewEvent.putExtra("image", events.get(position).getImgUrl());
            viewEvent.putExtra("placeId", events.get(position).getPlaceId());
            viewEvent.putExtra("venueName", events.get(position).getVenueName());
            viewEvent.putExtra("venueAddress", events.get(position).getVenueAddress());
            v.getContext().startActivity(viewEvent);
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
