package com.rafamaya.imagesearch.manager;

import com.rafamaya.imagesearch.provider.api.GoogleImageSearchProvider;
import com.rafamaya.imagesearch.provider.api.ImageBaseProvider.ImageListener;
import com.rafamaya.imagesearch.provider.api.ImageBaseProvider.ImageProvider;
import com.rafamaya.imagesearch.provider.api.LocalImageProvider;

public class ImageProviderApiManager implements ImageProvider{
	
	private EProviderType type;
	private String query;
	private int position;
	private GoogleImageSearchProvider googleProvider = new GoogleImageSearchProvider();
	private LocalImageProvider localProvider = new LocalImageProvider();
	
	
	public ImageProviderApiManager()
	{
		
	};
	
	
	public void setProviderType(EProviderType type)
	{
		this.type = type;
	}

	@Override
	public void loadImages() {
		if(type == EProviderType.GoogleSearch)
			googleProvider.loadImages();
		else
			localProvider.loadImages();
	}

	@Override
	public void setQuery(String query) {
		this.query = query;
		if(type == EProviderType.GoogleSearch)
			googleProvider.setQuery(query);
	}

	@Override
	public void setPosition(int position) {
		this.position = position;
		if(type == EProviderType.GoogleSearch)
			googleProvider.setPosition(position);
	}

	@Override
	public void cancel() {
		if(type == EProviderType.GoogleSearch)
			googleProvider.cancel();
		else
			localProvider.cancel();
	}

	@Override
	public void setListener(ImageListener listener) {
		if(type == EProviderType.GoogleSearch)
			googleProvider.setListener(listener);
		else
			localProvider.setListener(listener);
	}
	
	public enum EProviderType
	{
		GoogleSearch, Local
	};

}
