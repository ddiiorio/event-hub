package finalproject.comp3617.com.eventhub;

import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ImageHelper {

    public static void loadThumb(String imageUrl, ImageView img) {
        if (imageUrl != null && imageUrl.length() > 0) {
              Picasso.get()
                      .load(imageUrl)
                      .placeholder(R.drawable.placeholder)
                      .resize(300, 300)
                      .centerCrop()
                      .into(img);
        } else {
            Picasso.get().load(R.drawable.placeholder).into(img);
        }
    }

    public static void loadImage(String imageUrl, ImageView img) {
        if (imageUrl != null && imageUrl.length() > 0) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(img);
        } else {
            Picasso.get().load(R.drawable.placeholder).into(img);
        }
    }
}
