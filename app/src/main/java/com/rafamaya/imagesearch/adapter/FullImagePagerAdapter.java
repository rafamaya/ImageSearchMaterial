package com.rafamaya.imagesearch.adapter;

import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.rafamaya.imagesearch.R;
import com.rafamaya.imagesearch.fragment.FullImageViewFragment;
import com.rafamaya.imagesearch.model.SImage;

import java.util.ArrayList;
import java.util.HashMap;

public class FullImagePagerAdapter extends FragmentStatePagerAdapter {

    public interface ColorReadyListener
    {
        public void OnColorReady(String path);
    }
	
	private ArrayList<SImage> images = new ArrayList<SImage>();
    private FullImageViewFragment f;
    private String path;
    private ColorReadyListener colorListener;
    private DisplayImageOptions options;
    private HashMap<Integer, FullImageViewFragment> imageFragments = new HashMap<Integer,FullImageViewFragment>();

	public FullImagePagerAdapter(FragmentManager fm, ColorReadyListener listener) {
		super(fm);
		images.clear();
        imageFragments.clear();
        this.colorListener = listener;
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
	
	public void setImages(ArrayList<SImage> images)
	{
		this.images = images;
	}

	@Override
	public Fragment getItem(int position) {
		//Try using full image. If not available use thumbnail
        try {
            path = images.get(position).getImagePath() != null ? images.get(position).getImagePath() : images.get(position).getThumbnailPath();
            f = FullImageViewFragment.makeFragment(path, images.get(position).getDescription(), options);
            f.setListener(listener);
            imageFragments.put(position, f);
            return f;
        }catch (Exception ex){return f;}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return images.size();
	}

    public int getVibrantColor(String path)
    {
        return f.getVibrantColor(path);
    }

    private FullImageViewFragment.FullImageReadyListener listener = new FullImageViewFragment.FullImageReadyListener()
    {

        @Override
        public void OnImageReady(String path) {
            if(colorListener != null)
                colorListener.OnColorReady(path);
        }
    };

    public FullImageViewFragment getFragment(int position)
    {
        return imageFragments.get(position);
    }

}
