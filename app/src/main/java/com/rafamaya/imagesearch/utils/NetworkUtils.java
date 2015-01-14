package com.rafamaya.imagesearch.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.rafamaya.imagesearch.R;
import com.rafamaya.imagesearch.application.ImageSearchApplication;

public class NetworkUtils {
	
	public static Boolean hasNetworkConnection() {
		
		// get network info
		try
		{
			ConnectivityManager connectivityManager = (ConnectivityManager) ImageSearchApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			if(connectivityManager == null)
				return false;
			NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
			
			// if the device is connected via the mobile network or WIFI return true - otherwise return false
			if(activeNetworkInfo!=null && activeNetworkInfo.isConnected()) {
				return true;
			} else {
				return false;
			}
			
		}catch(Exception ex)
		{
			return false;
		}
	}
	
	public static boolean hasNetworkConnectionWithMessage()
	{
		//Show toast message if no internet connection
		if(!NetworkUtils.hasNetworkConnection())
		{
			Context ctx = ImageSearchApplication.getContext();
			Toast.makeText(ctx, ctx.getResources().getString(R.string.no_internet), Toast.LENGTH_LONG).show();
			return false;
		}
		
		return true;
	}

    public static boolean isBluetoothEnabled()
    {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            return false;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enable :)
                return false;
            }
        }
        return true;
    }

}
