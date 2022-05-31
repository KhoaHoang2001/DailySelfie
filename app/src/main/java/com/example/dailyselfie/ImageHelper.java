package com.example.dailyselfie;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class ImageHelper {

    public static void setImageFromFilePath(String imagePath, ImageView imageView, int targetW, int targetH) {
        BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
        bmpOptions.inJustDecodeBounds = true;
        int photoW = bmpOptions.outWidth;
        int photoH = bmpOptions.outHeight;

        int scaleFactor = Math.max(photoW / targetW, photoH / targetH);

        bmpOptions.inJustDecodeBounds = false;
        bmpOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmpOptions);
        imageView.setImageBitmap(bitmap);
    }

    public static void setImageFromFilePath(String imagePath, ImageView imageView) {
        setImageFromFilePath(imagePath, imageView, 120, 160);
    }
}