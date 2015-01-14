package com.rafamaya.imagesearch.provider.api;

import android.database.Cursor;
import android.provider.MediaStore;

import com.rafamaya.imagesearch.application.ImageSearchApplication;
import com.rafamaya.imagesearch.model.SImage;
import com.rafamaya.imagesearch.provider.api.ImageBaseProvider.ImageListener;
import com.rafamaya.imagesearch.provider.api.ImageBaseProvider.ImageProvider;

import java.util.ArrayList;

public class LocalImageProvider implements ImageProvider {
	
	private ImageListener listener;
	private ArrayList<SImage> images = new ArrayList<SImage>();
	
	private String[] imageColumns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME };
	private String orderBy = MediaStore.Images.Media._ID + " DESC"; //Order is descending order
    private final int MAX_IMAGES = 200;
	
	public LocalImageProvider()
	{
		
	};

	@Override
	public void loadImages() {
		getLocalImages();
	}

	@Override
	public void setQuery(String query) {

	}

	@Override
	public void setPosition(int position) {

	}

	@Override
	public void cancel() {

	}

	@Override
	public void setListener(ImageListener listener) {
		this.listener = listener;
	}
	
	private void getLocalImages()
	{
		Cursor imagecursor = null;
		try
		{
			//Query the SD card images and get the id and data associated.
			imagecursor = ImageSearchApplication.getContext().getContentResolver().
				query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns, null,
				null, orderBy);
			int imageColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media._ID);
			int count = imagecursor.getCount();
            if(count > MAX_IMAGES)
                count = MAX_IMAGES;
			
		    for (int i = 0; i < count; i++) {
			    imagecursor.moveToPosition(i);
			    int id = imagecursor.getInt(imageColumnIndex);
			    int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
			    int nameColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
	            
				SImage newImage = new SImage();
	
				//Set the image path. The path will be used by the image loader to display the image
				newImage.setImagePath(imagecursor.getString(dataColumnIndex));
				newImage.setDescription(imagecursor.getString(nameColumnIndex));
				
			    images.add(newImage);
			    
		    }
		    
		    imagecursor.close();
		}catch(Exception ex)
		{
			if(imagecursor != null)
				imagecursor.close();
		}
	    
	    if(listener != null)
	    	listener.OnImagesAvailable(images);
	}

}
