package com.dannydiiorio.eventhub;

import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Random;

public class ImageHelper {
    private static ArrayList<String> images = new ArrayList<>();

    static {
        images.add("https://firebasestorage.googleapis.com/v0/b/eventhub-208222.appspot.com/o/placeholders%2Fgeneral%2Fp1.png?alt=media&token=d6cfce52-866a-49aa-a42b-a6a78a32242f");
        images.add("https://firebasestorage.googleapis.com/v0/b/eventhub-208222.appspot.com/o/placeholders%2Fgeneral%2Fp2.png?alt=media&token=a2aa7e1b-b75a-47a2-b9e8-81b0736c4c3a");
        images.add("https://firebasestorage.googleapis.com/v0/b/eventhub-208222.appspot.com/o/placeholders%2Fgeneral%2Fp3.png?alt=media&token=9abd7bca-a919-4db9-a870-f1c80e9fec6b");
    }

    public static String getRandomPlaceholder() {
        Random rand = new Random();
        return images.get(rand.nextInt(images.size()));
    }

    public static void loadThumb(String imageUrl, ImageView img) {
        if (imageUrl != null && imageUrl.length() > 0) {
              Picasso.get()
                      .load(imageUrl)
                      .error(R.drawable.placeholder)
                      .resize(300, 300)
                      .centerCrop()
                      .into(img);
        } else {
            Picasso.get()
                    .load(R.drawable.placeholder)
                    .resize(300, 300)
                    .centerCrop()
                    .into(img);
        }
    }

    public static void loadImage(String imageUrl, ImageView img) {
        if (imageUrl != null && imageUrl.length() > 0) {
            Picasso.get()
                    .load(imageUrl)
                    .error(R.drawable.placeholder)
                    .into(img);
        } else {
            Picasso.get()
                    .load(R.drawable.placeholder)
                    .into(img);
        }
    }
}
