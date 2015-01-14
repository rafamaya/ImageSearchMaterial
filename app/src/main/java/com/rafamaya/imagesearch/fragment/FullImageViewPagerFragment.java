package com.rafamaya.imagesearch.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rafamaya.imagesearch.R;
import com.rafamaya.imagesearch.activity.MainSearchActivity;
import com.rafamaya.imagesearch.adapter.FullImagePagerAdapter;
import com.rafamaya.imagesearch.model.SImage;

import java.util.ArrayList;

public class FullImageViewPagerFragment extends Fragment{
	
	private final static String POS = "position";
	
	private ViewPager mPager;
	private ArrayList<SImage> images = new ArrayList<SImage>();
	private FullImagePagerAdapter adapter;
    private int currPosition;
    private int currentPage;
	
	public FullImageViewPagerFragment()
	{
        currPosition = 0;
	}
	
	public static Fragment makeFragment(int position)
	{
		FullImageViewPagerFragment f = new FullImageViewPagerFragment();
		Bundle args = new Bundle();
		
		//Open full image view on the thumbnail position that was clicked
		args.putInt(POS, position);
		f.setArguments(args);
		
		return f;
	}
	
	public void setImages(ArrayList<SImage> images)
	{
		this.images = images;
		if(adapter != null)
			adapter.notifyDataSetChanged();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_view_pager,
				container, false);
		
		//View pager to swipe full images
		mPager = (ViewPager)rootView.findViewById(R.id.pager);
		
		Bundle args = getArguments();

        //Check the position to assign the sharing url
        setPagerListener();

        currPosition = args.getInt(POS, 0);
		setupViewPager(args.getInt(POS, 0));
		
		return rootView;
	}
	
	private void setupViewPager(final int position)
	{
		adapter = new FullImagePagerAdapter(getChildFragmentManager(), new FullImagePagerAdapter.ColorReadyListener() {
            @Override
            public void OnColorReady(String path) {
                if(path.contentEquals(images.get(currPosition).getImagePath())) {
                    try {
                        if(adapter != null && images != null) {
                            ((MainSearchActivity) getActivity()).setToolbarColor(adapter.getVibrantColor(images.get(currPosition).getImagePath()));
                        }
                    }catch (Exception ex){}
                }
                if((MainSearchActivity)getActivity() != null && ((MainSearchActivity)getActivity()).isSeeingImagesOnWear())
                    ((MainSearchActivity)getActivity()).sendCurrentBitmap();
            }
        });
		adapter.setImages(images);
		mPager.setAdapter(adapter);

		adapter.notifyDataSetChanged();
		
		adapter.getItem(position);
		
		mPager.setCurrentItem(position);
	}
	
	private void setPagerListener()
	{
		mPager.setOnPageChangeListener(new OnPageChangeListener(){

			@Override
			public void onPageScrollStateChanged(int arg0) {}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}

			@Override
			public void onPageSelected(int position) {
				//Create Share Intent per image
                currPosition = position;
				((MainSearchActivity)getActivity()).setShareIntent(getShareIntent(position));
                ((MainSearchActivity)getActivity()).setToolbarColor(adapter.getVibrantColor(images.get(position).getImagePath()));
			}
			
		});
	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		((MainSearchActivity)getActivity()).setShareVisibility(false);
        ((MainSearchActivity)getActivity()).resetToolbarColor();
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		((MainSearchActivity)getActivity()).setShareVisibility(true);
	}
	
	
	private Intent getShareIntent(int position)
	{
		//Create share intent
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/*");
		try{

		//Use the image url
		shareIntent.putExtra(Intent.EXTRA_TEXT, images.get(position).getImagePath());
		
		}catch(Exception ex)
		{
		}
		
		return shareIntent;
	}

    public Bitmap getCurrentBitmap()
    {
        FullImageViewFragment f = (FullImageViewFragment)adapter.getFragment(currPosition);
        if(f!= null)
            return f.getCurrentBitmap();
        return null;
    }

    public void moveNext()
    {
        if(currPosition + 1 < adapter.getCount())
            mPager.setCurrentItem(currPosition + 1);
    }

    public void movePrev()
    {
        if(currPosition - 1 >= 0)
            mPager.setCurrentItem(currPosition - 1);
    }

}
