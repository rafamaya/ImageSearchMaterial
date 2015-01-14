package com.rafamaya.imagesearch.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.view.View;

import com.google.android.gms.wearable.Asset;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by rmaya2 on 12/17/2014.
 */
public class BitmapUtils {

    public Bitmap autoRotateBitmap(Bitmap source, String file, boolean isLocalFile, boolean recycle)
    {
        if (!isLocalFile)
            return source;
        Bitmap result = source;
        try {
            ExifInterface exif = new ExifInterface(file);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }

            result = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true); // rotating bitmap
            if (result != source && recycle) {
                source.recycle();
            }
        }
        catch (Exception e) {

        }

        return result;
    }

    public Bitmap scaleBitmap(Bitmap source, View view, boolean recycle)
    {
        try {
            int bwidth = source.getWidth();
            int bheight = source.getHeight();
            int swidth = view.getWidth();
            int sheight = view.getHeight();
            int new_width = swidth;
            int new_height = (int) Math.floor((double) bheight * ((double) new_width / (double) bwidth));
            Bitmap result = Bitmap.createScaledBitmap(source, new_width, new_height, true);
            if (result != source && recycle) {
                source.recycle();
            }
            return result;
        }catch (Exception ex)
        {
            return source;
        }
    }

    public void getImageDetails(String filePath)
    {
        try {
            ExifInterface exif = new ExifInterface(filePath);
            exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }

    public static Asset toAsset(Bitmap bitmap) {
        ByteArrayOutputStream byteStream = null;
        try {
            byteStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            return Asset.createFromBytes(byteStream.toByteArray());
        } finally {
            if (null != byteStream) {
                try {
                    byteStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

        int width = bm.getWidth();
        int height = bm.getHeight();

        int new_width = newWidth;
        int new_height = (int) Math.floor((double) width * ((double) new_width / (double) width));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bm, new_width, new_height, true);

//        float scaleWidth = ((float) newWidth) / width;
//        float scaleHeight = ((float) newHeight) / height;
//
//        Matrix matrix = new Matrix();
//        matrix.postScale(scaleWidth, scaleHeight);
//
//        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

        return resizedBitmap;

    }
}
