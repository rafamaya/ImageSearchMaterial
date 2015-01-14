package com.rafamaya.imagesearch.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.rafamaya.imagesearch.R;
import com.rafamaya.imagesearch.activity.MainSearchActivity;
import com.rafamaya.imagesearch.adapter.GridViewImageAdapter;
import com.rafamaya.imagesearch.listeners.EndlessScrollListener;
import com.rafamaya.imagesearch.model.SImage;

import java.util.ArrayList;

public class ImageGridViewFragment extends Fragment{
	
	private View rootView;
	private GridView gv;
	private ArrayList<SImage> images = new ArrayList<SImage>();
	private GridViewImageAdapter adapter;

	public ImageGridViewFragment() {
		// TODO Auto-generated constructor stub
	}
	
	public static Fragment makeFragment()
	{
		ImageGridViewFragment fragment = new ImageGridViewFragment();
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		rootView = inflater.inflate(R.layout.fragment_grid_view,
				container, false);
		
		setupGridView();
		
		return rootView;
	}
	
	public void setImages(ArrayList<SImage> images)
	{
		this.images.clear();
		this.images.addAll(images);
		
		if(adapter != null)
			adapter.notifyDataSetChanged();
	}
	
	private void setupGridView()
	{
		//3 column grid view
		gv = (GridView) rootView.findViewById(R.id.grid_view);
		
		adapter = new GridViewImageAdapter(getActivity());
		adapter.setImageUrls(images);
	    gv.setAdapter(adapter);
	    adapter.notifyDataSetChanged();
	    
	    gv.setOnScrollListener(new EndlessScrollListener(){

			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				//Get more images if user is scrolling at the bottom of the screen
				if(page > 0 && totalItemsCount > 0)
					((MainSearchActivity)getActivity()).loadMore(page);
			}
	    	
	    });
	    
	    gv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				
				//Launch full image view when individual thumbnail is clicked
				((MainSearchActivity)getActivity()).launchFullImageViewPagerFragment(pos);
			}
	    	
	    });
	}

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainSearchActivity)getActivity()).resetToolbarColor();
    }
}