package com.rafamaya.imagesearch.provider.api;

import java.util.ArrayList;
import java.util.HashMap;

import com.rafamaya.imagesearch.model.SImage;

public class ImageBaseProvider {
	
	//Provide results back so they can be displayed
	public interface ImageListener
	{
		public void OnImagesAvailable(ArrayList<SImage> images);
		public void OnMoreResultsAvailable(HashMap<Integer, Integer> moreResulstMap);
	};
	
	//Each provider implements these interface methods (e.g. local data vs google image search)
	public interface ImageProvider
	{
		public void loadImages();
		
		public void setQuery(String query);
		
		public void setPosition(int position);
		
		public void cancel();
		
		public void setListener(ImageListener listener);
	};

}
