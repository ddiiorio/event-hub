package finalproject.comp3617.com.eventhub.Realm;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Event extends RealmObject {
    @PrimaryKey
    private String id;
    private String title;
    private String imgUrl;
    private String placeId;
    private String venueName;
    private String venueAddress;
    private Date eventDate;

    public Event() {  }

    public Event(String id) {
        this.id = id;
    }

    public Event(String title, String imgUrl, Date eventDate) {
        this.title = title;
        this.imgUrl = imgUrl;
        this.eventDate = eventDate;
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

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }
}
