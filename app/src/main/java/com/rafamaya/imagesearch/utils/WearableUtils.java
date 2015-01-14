package com.rafamaya.imagesearch.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.rafamaya.imagesearch.R;
import com.rafamaya.imagesearch.activity.MainSearchActivity;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Created by rmaya2 on 1/8/2015.
 */
public class WearableUtils implements DataApi.DataListener,
        MessageApi.MessageListener, NodeApi.NodeListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    public interface WearListener
    {
        public void OnSearchWord(String word);
        public void OnSendCurrentBitmap();
        public void OnGoNext();
        public void OnGoPrev();
    }

    private Context context;

    private NotificationManager notificationManager;

    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String IMAGE_PATH = "/image";
    private static final String IMAGE_KEY = "photo";

    private WearListener listener;
    private static final int REQUEST_RESOLVE_ERROR = 1000;

    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;
    private Handler mHandler;
    private boolean isSeeingImagesOnWear = false;
    private boolean searchedImageWithWear = false;


    public WearableUtils(Context ctx, WearListener listener)
    {
        context = ctx;
        notificationManager  = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mHandler = new Handler();
        this.listener = listener;
        launchOnWear();
    }

    public void launchOnWear()
    {
        if(mGoogleApiClient == null)
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

        if (!mResolvingError && mGoogleApiClient != null && !mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }else if(!mResolvingError && mGoogleApiClient.isConnected())
        {
            listener.OnSendCurrentBitmap();
        }
    }

    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }

        return results;
    }

    private void sendStartActivityMessage(String node) {
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, START_ACTIVITY_PATH, new byte[0]).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        Status status = sendMessageResult.getStatus();
                        if (!status.isSuccess()) {
                            int i = 0;
                        }
                    }
                }
        );
    }

    public void onStartWearableActivityClick(View view) {
        new StartWearableActivityTask().execute();
    }

    private class StartWearableActivityTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                sendStartActivityMessage(node);
            }
            return null;
        }
    }

    private void sendPhoto(Asset asset) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(IMAGE_PATH);
        dataMap.getDataMap().putAsset(IMAGE_KEY, asset);
        dataMap.getDataMap().putLong("time", new Date().getTime());
        PutDataRequest request = dataMap.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        Status status = dataItemResult.getStatus();
                        status.getStatusCode();
                    }
                });

    }

    public void sendCurrentPhoto(Bitmap bitmap)
    {
        Bitmap resized = BitmapUtils.getResizedBitmap(bitmap, 230,230);
        if (null != resized && mGoogleApiClient.isConnected()) {
            sendPhoto(BitmapUtils.toAsset(resized));
        }
    }

    @Override //ConnectionCallbacks
    public void onConnected(Bundle connectionHint) {
        mResolvingError = false;
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.NodeApi.addListener(mGoogleApiClient, this);

        onStartWearableActivityClick(null);

    }

    @Override //ConnectionCallbacks
    public void onConnectionSuspended(int cause) {
    }

    @Override //OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult((MainSearchActivity)context, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            mResolvingError = false;
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            Wearable.NodeApi.removeListener(mGoogleApiClient, this);
        }
    }

    @Override //DataListener
    public void onDataChanged(DataEventBuffer dataEvents) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();
    }

    @Override //MessageListener
    public void onMessageReceived(final MessageEvent messageEvent) {
        Log.d("Wear", "onMessageReceived() A message from watch was received:" + messageEvent
                .getRequestId() + " " + messageEvent.getPath());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(messageEvent.getPath().equals("/startImage"))
                    isSeeingImagesOnWear = true;

                if(messageEvent.getPath().equals("/search"))
                {
                    searchedImageWithWear = true;
                    String word = null;
                    try {
                        word = new String(messageEvent.getData().clone(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    isSeeingImagesOnWear = true;
                    listener.OnSearchWord(word);
                }
                else if(isSeeingImagesOnWear) {
                    if(messageEvent.getPath().equals("/startImage")){
                        listener.OnSendCurrentBitmap();
                    }
                    else if (messageEvent.getPath().equals("/next")) {
                        listener.OnGoNext();
                    } else if (messageEvent.getPath().equals("/prev")) {
                        listener.OnGoPrev();
                    }
                }
            }
        });

    }

    @Override //NodeListener
    public void onPeerConnected(final Node peer) {}

    @Override //NodeListener
    public void onPeerDisconnected(final Node peer) {}

    public void stop()
    {
        if (!mResolvingError && mGoogleApiClient != null) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            Wearable.NodeApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    public boolean isSearchedImageWithWear()
    {
        return searchedImageWithWear;
    }

    public boolean isSeeingImagesOnWear()
    {
        return isSeeingImagesOnWear;
    }

    public void setIsSeeingImagesOnWear(boolean isSeeing){
        isSeeingImagesOnWear = isSeeing;
    }

    public void clearWearState()
    {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected())
        {
            mGoogleApiClient.disconnect();
        }
        isSeeingImagesOnWear = false;
        searchedImageWithWear = false;
    }

    private void sendInitialNotificationToWear()
    {
        int dot = 200;      // Length of a Morse Code "dot" in milliseconds
        int dash = 500;     // Length of a Morse Code "dash" in milliseconds
        int short_gap = 200;    // Length of Gap Between dots/dashes
        int medium_gap = 500;   // Length of Gap Between Letters
        int long_gap = 1000;    // Length of Gap Between Words
        long[] pattern = {
                0,  // Start immediately
                dot, short_gap, dot, short_gap, dot,    // s
                medium_gap
        };


        Intent intentWear = new Intent("ViewOnWear");
        PendingIntent pIntentWear = PendingIntent.getBroadcast(context, 0, intentWear, 0);

        Intent intentWearSearch = new Intent("SearchOnWear");
        PendingIntent pIntentWearSearch = PendingIntent.getBroadcast(context, 0, intentWearSearch, 0);

        Notification noti = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("View Images")
                .setVibrate(pattern)
                .setAutoCancel(false)
                .setStyle(new Notification.BigTextStyle().bigText("View or Search images and sync navigation!"))
                .setLargeIcon(BitmapFactory.decodeResource(
                        context.getResources(), R.drawable.ic_launcher))
                .addAction(android.R.drawable.ic_menu_gallery, "View", pIntentWear)
                .addAction(android.R.drawable.ic_menu_search, "Search", pIntentWearSearch)
                .build();

        // hide the notification after its selected
        //noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, noti);

        IntentFilter filter = new IntentFilter();
        filter.addAction("ViewOnWear");
        filter.addAction("SearchOnWear");

        context.registerReceiver(wearReceiver, filter);

    }

    private WearBroadcastReceiver wearReceiver = new WearBroadcastReceiver();

    private class WearBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.unregisterReceiver(wearReceiver);
            if(intent.getAction().contentEquals("ViewOnWear"))
            {

                //notificationManager.cancel(0);
            }
            else if(intent.getAction().contentEquals("SearchOnWear"))
            {

                //notificationManager.cancel(0);
            }
        }
    };

}
