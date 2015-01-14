package com.rafamaya.imagesearch.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rafamaya.imagesearch.R;
import com.rafamaya.imagesearch.fragment.FullImageViewPagerFragment;
import com.rafamaya.imagesearch.fragment.ImageGridViewFragment;
import com.rafamaya.imagesearch.fragment.ImageSearchFragment;
import com.rafamaya.imagesearch.manager.ImageProviderApiManager;
import com.rafamaya.imagesearch.manager.ImageProviderApiManager.EProviderType;
import com.rafamaya.imagesearch.model.SImage;
import com.rafamaya.imagesearch.provider.SearchHistorySuggestionProvider;
import com.rafamaya.imagesearch.provider.api.ImageBaseProvider.ImageListener;
import com.rafamaya.imagesearch.utils.NetworkUtils;
import com.rafamaya.imagesearch.utils.WearableUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class MainSearchActivity extends ActionBarActivity implements SearchView.OnQueryTextListener{
	
	private final static int MIN_LETTERS = 1;
	
	private SearchView mSearchView;
	private ImageGridViewFragment gridViewFragment;
	private FullImageViewPagerFragment fullImageFragment;
	
	private ImageProviderApiManager imageProviderMgr = new ImageProviderApiManager();
	private SearchRecentSuggestions historySuggestions = new SearchRecentSuggestions(this,
			SearchHistorySuggestionProvider.AUTHORITY, SearchHistorySuggestionProvider.MODE);
	
	private ArrayList<SImage> images = new ArrayList<SImage>();
	private HashMap<Integer, Integer> moreResulstMap = new HashMap<Integer, Integer>();
	private String query = "";
	private MenuItem searchItem;
	private MenuItem shareItem;
	private MenuItem clearHistoryItem;
    private MenuItem wearItem;
	
	private ShareActionProvider mShareActionProvider;

    private Toolbar toolbar;

    private Handler mHandler;
    private WearableUtils wearableUtils;
    private int isShowingSearchCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_search);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            resetToolbarColor();
        }

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, ImageSearchFragment.makeFragment()).commit();
		}
		
		handleIntent(getIntent());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_search, menu);
		
		searchItem = menu.findItem(R.id.action_search);

        mSearchView = new SearchView(getSupportActionBar().getThemedContext());
        mShareActionProvider = new ShareActionProvider(getSupportActionBar().getThemedContext());
		
		clearHistoryItem = menu.findItem(R.id.action_clear_hist);
		shareItem = menu.findItem(R.id.menu_item_share);
        wearItem = menu.findItem(R.id.action_see_on_wear);
		
		return true;
	}
	
   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onPrepareOptionsMenu(menu);

	    MenuItemCompat.setActionView(searchItem, mSearchView);
	    setupSearchView(searchItem);

        MenuItemCompat.setActionProvider(shareItem, mShareActionProvider);
        setShareVisibility(false);

        boolean isBluetoothEnabled = NetworkUtils.isBluetoothEnabled();
        wearItem.setVisible(isBluetoothEnabled);
        wearItem.setEnabled(isBluetoothEnabled);
	    
	    return true;
    }
	
	private void setupSearchView(MenuItem searchItem) {
		 
		//Show the full search view widget
	    searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        if(mSearchView != null) {
            mSearchView.setQueryHint(getResources().getString(R.string.image_search_hint));
            mSearchView.setIconifiedByDefault(false);
            mSearchView.setSubmitButtonEnabled(true);

            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            if (searchManager != null) {

                mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            }

            mSearchView.setOnQueryTextListener(this);
        }
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		
		if(id == R.id.action_clear_hist)
		{
			clearHistory();
            clearCache();
            clearWearState();
		}

        if(id == R.id.action_see_on_wear)
        {
            mHandler = new Handler();
            launchOnAndroidWear();
        }
		
		return super.onOptionsItemSelected(item);
	}
	

	@Override
	protected void onPause() {
		//Cancel currently running image search tasks in case activity will be removed
		if(imageProviderMgr != null)
			imageProviderMgr.cancel();
		super.onPause();
	}

    @Override
    protected void onStop() {
        if(wearableUtils != null)
            wearableUtils.stop();
        super.onStop();
    }

    @Override
	public boolean onQueryTextChange(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		
		clearState();
		
		this.query = query;
		
		//Warn user if query is shorter than 1 letters
		if(query.length() < MIN_LETTERS)
		{
			Toast.makeText(this, getResources().getString(R.string.enter_valid_word), Toast.LENGTH_LONG).show();
			return false;
		}
		
		hidesearchKeyboard();
		
		//Search the first page
        searchImages(query, 0);
        
		return true;
	}
	
	private void hidesearchKeyboard()
	{
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        clearSearchFocus();
	}
	
	private void clearSearchFocus()
	{
	    mSearchView.clearFocus();
	    mSearchView.setFocusable(false);
	}

	public void searchImages(String query, int page)
	{
		if(!NetworkUtils.hasNetworkConnectionWithMessage())
			return;

		if(fullImageFragment != null)
		{
			getSupportFragmentManager().beginTransaction().remove(fullImageFragment).commit();

			fullImageFragment = null;
		}
		loadQueryImages(page);

		//Provide search history
	    saveQueryToHistory(query);

        setTitle(query);
	}
	
	public void loadMore(int page)
	{
		try
		{
			if(moreResulstMap.size() > 0)
			{
				int start = moreResulstMap.get(page);
				loadQueryImages(start);
			}
		}catch(Exception ex)
		{
			//loadQueryImages(0);
		}
	}
	
	private void loadQueryImages(int position)
	{
		imageProviderMgr.setProviderType(EProviderType.GoogleSearch);
		imageProviderMgr.setListener(imageListener);
		imageProviderMgr.setQuery(query);
		imageProviderMgr.setPosition(position);
		imageProviderMgr.loadImages();
	}
	
	private void loadLocalImages(int position)
	{
		imageProviderMgr.setProviderType(EProviderType.Local);
		imageProviderMgr.setListener(imageListener);
		imageProviderMgr.setQuery("");
		imageProviderMgr.setPosition(position);
		imageProviderMgr.loadImages();
	}
	
	public void loadLocalImages()
	{
        clearState();
		loadLocalImages(0);
	}
	
	//Listener of the Image Provider API
	private ImageListener imageListener = new ImageListener()
	{

		@Override
		public void OnImagesAvailable(ArrayList<SImage> mimages) {
			try
			{
				if(images.size() > 0 && mimages.size() > 0)
					images.addAll(images.size(), mimages);
				else if(mimages.size() > 0)
					images.addAll(mimages);
                if(getSupportFragmentManager().findFragmentByTag("gridFrag") == null) {
                    if(gridViewFragment == null || images.size() == 0)
                    {
                        gridViewFragment = (ImageGridViewFragment) ImageGridViewFragment.makeFragment();
                    }

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, gridViewFragment, "gridFrag").addToBackStack(null).commit();

                    gridViewFragment.setImages(images);
                }else
                    gridViewFragment.setImages(images);

			}catch(Exception ex)
			{
				
			}

            if(wearableUtils != null && wearableUtils.isSearchedImageWithWear()) {
                mHandler.postDelayed(new Runnable() {
                      @Override
                      public void run() {
                          if(isShowingSearchCount == 0 ) {
                              launchFullImageViewPagerFragment(0);
                              isShowingSearchCount++;
                          }else
                            isShowingSearchCount = 0;
                      }
                }, 1000);

            }
			
		}

		@Override
		public void OnMoreResultsAvailable(
				HashMap<Integer, Integer> _moreResulstMap) {
			if(_moreResulstMap != null)
				moreResulstMap = _moreResulstMap;
		}
		
	};
	
	private void saveQueryToHistory(String query)
	{
		historySuggestions.saveRecentQuery(query, null);
	}
	
	private void clearHistory()
	{
		historySuggestions.clearHistory();
	}

    public void clearCache()
    {
        ImageLoader.getInstance().clearDiskCache();
        ImageLoader.getInstance().clearMemoryCache();
    }
	
	@Override
	protected void onNewIntent(Intent intent) {
	    setIntent(intent);
	    handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		//A new intent is delivered each time an item from search history is clicked or voice query is provided
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
		      String query = intent.getStringExtra(SearchManager.QUERY);
		      
		     //Warn user if query is shorter than 1 letters
		      if(query == null || query.length() < MIN_LETTERS)
		      {
		    	  Toast.makeText(this, getResources().getString(R.string.enter_valid_word), Toast.LENGTH_LONG).show();
		    	  return;
		      }

            search(query);

	    }
	}

    public void search(String query)
    {
        this.query = query;
        clearState();
        mSearchView.setQuery(query, false);
        clearSearchFocus();
        searchImages(query, 0);
    }
	
	private void clearState()
	{
	     images.clear();
	     if(moreResulstMap != null)
	    	 moreResulstMap.clear();
	     //gridViewFragment = null;
	}
	
	public void launchFullImageViewPagerFragment(int position)
	{
		 fullImageFragment = null;
		 fullImageFragment = (FullImageViewPagerFragment) FullImageViewPagerFragment.makeFragment(position);
		 fullImageFragment.setImages(images);
		 getSupportFragmentManager().beginTransaction()
            .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
			.add(R.id.container, fullImageFragment).
                 addToBackStack(null).commit();
		
	}
	
	public void setShareIntent(Intent shareIntent) {
	    if (mShareActionProvider != null) {
	        mShareActionProvider.setShareIntent(shareIntent);
	    }
	}
	
	public void setShareVisibility(boolean visible)
	{
		if(shareItem != null)
		{
			shareItem.setVisible(visible);
			shareItem.setEnabled(visible);
		}
		
		if(searchItem != null)
		{
			searchItem.setVisible(!visible);
			searchItem.setEnabled(!visible);
		}
		
		if(clearHistoryItem != null)
		{
			clearHistoryItem.setVisible(!visible);
			clearHistoryItem.setEnabled(!visible);
		}
	}

    public void setToolbarColor(int color)
    {
        toolbar.setBackgroundColor(color);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.primary_text_light));
    }

    public void resetToolbarColor()
    {
        toolbar.setBackgroundColor(getResources().getColor(R.color.primary_darker_color));
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.primary_text_dark));
    }

    public void setTitle(String title)
    {
        toolbar.setTitle(title);
    }


    //Implementation for Android Wear

    private void launchOnAndroidWear()
    {

        if(wearableUtils == null) {
            wearableUtils = new WearableUtils(this, new WearableUtils.WearListener() {
                @Override
                public void OnSearchWord(String word) {
                    search(word);
                }

                @Override
                public void OnSendCurrentBitmap() {
                    if(fullImageFragment != null)
                        sendCurrentBitmap();
                }

                @Override
                public void OnGoNext() {
                    if(fullImageFragment != null)
                        fullImageFragment.moveNext();
                }

                @Override
                public void OnGoPrev() {
                    if(fullImageFragment != null)
                        fullImageFragment.movePrev();
                }
            });
        }else
            wearableUtils.launchOnWear();
    }

    public boolean isSeeingImagesOnWear()
    {
        try {
            if (wearableUtils != null)
                return wearableUtils.isSeeingImagesOnWear();
            return false;
        }catch (Exception ex){
            return false;
        }
    }

    private void clearWearState()
    {
        if(wearableUtils != null)
            wearableUtils.clearWearState();
    }

    public void sendCurrentBitmap()
    {
        if(wearableUtils != null)
            wearableUtils.setIsSeeingImagesOnWear(true);
        if(fullImageFragment != null && wearableUtils != null)
        {
            Bitmap b = fullImageFragment.getCurrentBitmap();
            if(b != null) {
                wearableUtils.sendCurrentPhoto(b);
            }

        }
    }

}
