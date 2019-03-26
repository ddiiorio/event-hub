package finalproject.comp3617.com.eventhub.Model;

import java.io.Serializable;
import java.util.Objects;

public class Event implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String title;
    private String imgUrl;
    private String placeId;
    private String venueName;
    private String venueAddress;
    private String eventDate;
    private long eventDateMillis;

    public Event() {  }

    public Event(String id) {
        this.id = id;
    }

    public Event(String title, String imgUrl, String eventDate) {
        this.title = title;
        this.imgUrl = imgUrl;
        this.eventDate = eventDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return eventDateMillis == event.eventDateMillis &&
                Objects.equals(title, event.title) &&
                Objects.equals(imgUrl, event.imgUrl) &&
                Objects.equals(placeId, event.placeId) &&
                Objects.equals(venueName, event.venueName) &&
                Objects.equals(venueAddress, event.venueAddress) &&
                Objects.equals(eventDate, event.eventDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, imgUrl, placeId, venueName,
                venueAddress, eventDate, eventDateMillis);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public String getVenueAddress() {
        return venueAddress;
    }

    public void setVenueAddress(String venueAddress) {
        this.venueAddress = venueAddress;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public long getEventDateMillis() {
        return eventDateMillis;
    }

    public void setEventDateMillis(long eventDateMillis) {
        this.eventDateMillis = eventDateMillis;
    }
}
