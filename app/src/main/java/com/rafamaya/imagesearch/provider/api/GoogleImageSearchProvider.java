package com.rafamaya.imagesearch.provider.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.rafamaya.imagesearch.model.SImage;
import com.rafamaya.imagesearch.provider.api.ImageBaseProvider.ImageListener;
import com.rafamaya.imagesearch.provider.api.ImageBaseProvider.ImageProvider;

public class GoogleImageSearchProvider implements ImageProvider{
	
	//Google Image Search API (Depricated but free). 
	private static final String BASE_SEARCH_URL = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=";
	
	private ImageSearchTask searchTask;
	
	private ImageListener listener;
	private String query;
	private int position;
	private ArrayList<SImage> images = new ArrayList<SImage>();
	private HashMap<Integer, Integer> moreResulstMap = new HashMap<Integer, Integer>();
	
	public GoogleImageSearchProvider()
	{
		
	}
	
	public void loadImages() {
		searchImages(query, position);
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public void cancel() {
		if(searchTask != null)
			searchTask.cancel(true);
	}

	public void setListener(ImageListener listener) {
		this.listener = listener;
	}
	
	private void searchImages(String query, int page)
	{
		images.clear();
		searchTask = new ImageSearchTask();
		searchTask.execute(query, Integer.toString(page));
	}

	
	public static JSONObject searchGoogleImages(String query, String page)
	{

		HttpURLConnection connection = null;  
		StringBuilder builder = new StringBuilder();
		
		String thepage = page != null ? "&start=" + page : "";
		
		try{
			//Obtain 8 images per page. A max of 64 total images per search.
			URL url = new URL(BASE_SEARCH_URL + URLEncoder.encode(query, "utf8") + thepage + "&rsz=8");
			connection = (HttpURLConnection)url.openConnection();
			connection.addRequestProperty("Referer", "Image Search Test");
	        InputStreamReader in = new InputStreamReader(connection.getInputStream());
	
	        // Load the results into a StringBuilder
	        int read;
	        char[] buff = new char[1024];
	        while ((read = in.read(buff)) != -1) {
	        	builder.append(buff, 0, read);
	        }
	        
		    } catch (MalformedURLException ex) {
	
		        return null;
		    } catch (IOException ex) {
	
		        return null;
		    } finally {
		        if (connection != null) {
		        	connection.disconnect();
		        }
		    }
	
	        JSONObject jsonObject = new JSONObject();
	        try {
	            jsonObject = new JSONObject(builder.toString());
	        } catch (JSONException e) {
	
	            e.printStackTrace();
	        }
	        
	    return jsonObject;
	 }
	 
	 public class ImageSearchTask extends AsyncTask <String, Void, JSONObject>
	 {

		@Override
		protected JSONObject doInBackground(String... args) {
			String query = args[0];
			String page = args[1];
			//Use page to load more images using the same query
			JSONObject json = searchGoogleImages(query, page);
			
			return json;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			
			if(listener != null)
			{
				if(result == null)
					listener.OnImagesAvailable(images);
				else
				{
					processJson(result);
					listener.OnMoreResultsAvailable(moreResulstMap);
					listener.OnImagesAvailable(images);
				}
			}
				
		}
		
		private void processJson(JSONObject json)
		{
			try
			{
				JSONObject response = json.getJSONObject("responseData");
				JSONArray results = response.getJSONArray("results");
				
				for(int i = 0; i < results.length(); i++)
				{
					JSONObject result = results.getJSONObject(i);
					
					//Use thumbnail url for grid view and full url for full image view
					SImage newImage = new SImage();
					newImage.setThumbnailPath(result.getString("tbUrl"));
					newImage.setImagePath(result.getString("url"));
					newImage.setDescription(result.getString("contentNoFormatting"));
					
					images.add(newImage);
				}
				
				JSONObject cursor = response.getJSONObject("cursor");
				if(cursor != null)
				{
					//Get pages array mapping to load more images when scrolling down (max of 64 images)
					JSONArray pages = cursor.getJSONArray("pages");
					for(int i = 0; i < pages.length(); i++)
					{
						moreResulstMap.put(pages.getJSONObject(i).getInt("label"), pages.getJSONObject(i).getInt("start"));
					}
				}
			}catch(Exception ex)
			{
				
			}
		}
		 
	 }

}
