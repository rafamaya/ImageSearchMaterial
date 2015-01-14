/*Image Downloading using Picasso https://github.com/square/picasso/
 * 
Copyright 2013 Square, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.rafamaya.imagesearch.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.rafamaya.imagesearch.R;
import com.rafamaya.imagesearch.model.SImage;
import com.rafamaya.imagesearch.utils.BitmapUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GridViewImageAdapter extends BaseAdapter {

	private final Context context;
	private List<SImage> images = new ArrayList<SImage>();
    private DisplayImageOptions options;
    private BitmapUtils bitmapUtils = new BitmapUtils();

	public GridViewImageAdapter(Context ctx) {
		this.context = ctx;
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.placeholder) // resource or drawable
                .showImageForEmptyUri(R.drawable.placeholder) // resource or drawable
                .showImageOnFail(R.drawable.placeholder) // resource or drawable
                .resetViewBeforeLoading(false)  // default
                .delayBeforeLoading(0)
                .cacheInMemory(true) // default
                .cacheOnDisk(true) // default
                .considerExifParams(false) // default
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new SimpleBitmapDisplayer()) // default
                .handler(new Handler()) // default
                .build();
	}
	
	public void setImageUrls(ArrayList<SImage> images)
	{
		this.images = images;
	}

	@Override 
	public View getView(int position, View view, ViewGroup viewGroup) {
	   
//		SquaredImageView view = (SquaredImageView) convertView;
//
//		if (view == null) {
//			view = new SquaredImageView(context);
//			view.setScaleType(ScaleType.CENTER_CROP);
//			//Give some padding to each thumbnail image displayed on the grid view
//			view.setPadding(5, 5, 5, 5);
//		}


        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.grid_item, viewGroup, false);
        }

        final ImageView image = (ImageView) view.findViewById(R.id.image);
        final TextView text = (TextView) view.findViewById(R.id.text);

		// Get the image path for the current position.
		SImage simage = getItem(position);
		
		//try getting thumbnail. If not available, use full image
		final String url = simage.getThumbnailPath()!= null ? simage.getThumbnailPath() : simage.getImagePath();

        final boolean isLocalFile = !url.startsWith("http");

        ImageLoader.getInstance().displayImage(!isLocalFile ? url : "file://" + (new File(url)).getAbsolutePath(), image, options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                image.setImageBitmap(null);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                image.setImageBitmap(bitmapUtils.autoRotateBitmap(loadedImage, url, isLocalFile, false));
                try {
                    Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                    Palette.generateAsync(bitmap,
                            new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {
                                    Palette.Swatch vibrant =
                                            palette.getVibrantSwatch();
                                    if (vibrant != null) {
                                        text.setBackgroundColor(
                                                vibrant.getRgb());
                                    }
                                }
                            });
                }catch (Exception ex){}
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });

		return view;
	}

	@Override 
	public int getCount() {
		return images.size();
	}

	@Override 
	public SImage getItem(int position) {
		return images.get(position);
	}

	@Override 
	public long getItemId(int position) {
		return position;
	}

}
