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

package com.rafamaya.imagesearch.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.rafamaya.imagesearch.R;
import com.rafamaya.imagesearch.utils.BitmapUtils;

import java.io.File;
import java.util.HashMap;

public class FullImageViewFragment extends Fragment {

    public interface FullImageReadyListener{
        public void OnImageReady(String path);
    }

	private final static String IMAGE_PATH = "img_path";
	private final static String IMAGE_DESC = "img_desc";
	private ImageView ivImage;
	private TextView descTextView;
    private LinearLayout mainLayout;
	private String imagePath;
	private String imageDesc;
    private ProgressBar progress;
    private int color = 0;
    private static HashMap<String, Integer> colors = new HashMap<String, Integer>();
    private FullImageReadyListener listener;
    private static DisplayImageOptions options;
    private BitmapUtils bitmapUtils = new BitmapUtils();


	public static FullImageViewFragment makeFragment(String imagePath, String imageDesc, DisplayImageOptions theoptions) {
		
		FullImageViewFragment f = new FullImageViewFragment();
		Bundle args = new Bundle();
		
		//Use the full image path instead of the thumbnail
		args.putString(IMAGE_PATH, imagePath);
		args.putString(IMAGE_DESC, imageDesc);
		f.setArguments(args);
        options = theoptions;
		
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		Bundle arg = getArguments();
		imagePath = arg.getString(IMAGE_PATH, "");
		imageDesc = arg.getString(IMAGE_DESC, "");
		
		View root = inflater.inflate(R.layout.fragment_full_image_view, container, false);
        mainLayout = (LinearLayout)root.findViewById(R.id.mainLayout);
		ivImage = (ImageView) root.findViewById(R.id.imageview);
        progress = (ProgressBar)root.findViewById(R.id.progress);
		descTextView = (TextView) root.findViewById(R.id.description);
		descTextView.setText(imageDesc);
		setFullImage();
		
		return root;
	}

    public void setListener(FullImageReadyListener listener)
    {
        this.listener = listener;
    }

	public void setFullImage() {
		try {

            final boolean isLocalFile = !imagePath.startsWith("http");
            ImageLoader.getInstance().displayImage(!isLocalFile ? imagePath : "file://" + (new File(imagePath)).getAbsolutePath(), ivImage, options, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    progress.setIndeterminate(true);
                    progress.setVisibility(View.VISIBLE);
                    ivImage.setImageBitmap(null);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    progress.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    ivImage.setImageBitmap(bitmapUtils.scaleBitmap(bitmapUtils.autoRotateBitmap(loadedImage, imagePath, isLocalFile, false), view, true));

                    try {
                        Bitmap bitmap = ((BitmapDrawable) ivImage.getDrawable()).getBitmap();
                        Palette.generateAsync(bitmap,
                                new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(Palette palette) {
                                        Palette.Swatch vibrant =
                                                palette.getVibrantSwatch();
                                        Palette.Swatch vibrantText =
                                                palette.getLightVibrantSwatch();
                                        Palette.Swatch vibrant2Text =
                                                palette.getLightMutedSwatch();
                                        if (vibrantText != null || vibrant2Text != null) {
                                            color = vibrantText != null ? vibrantText.getRgb() : vibrant2Text.getRgb();
                                            colors.put(imagePath, color);
                                            if (listener != null)
                                                listener.OnImageReady(imagePath);
                                            descTextView.setTextColor(
                                                    color);

                                        }
                                        if (vibrant != null)
                                            mainLayout.setBackgroundColor(vibrant.getRgb());
                                    }
                                });
                    }catch (Exception ex){}

                    progress.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    progress.setVisibility(View.GONE);
                }
            });
			
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			System.gc();
            progress.setVisibility(View.GONE);
		}
	}



	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

    public int getVibrantColor(String path)
    {
        return colors.get(path) != null ? colors.get(path) : 0;
    }

    public Bitmap getCurrentBitmap()
    {
        Bitmap bitmap = ((BitmapDrawable) ivImage.getDrawable()).getBitmap();
        return bitmap;
    }

}
