package com.rafamaya.imagesearch;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.wearable.view.DelayedConfirmationView;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.List;

public class PhotoActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener, MessageApi.MessageListener,
        NodeApi.NodeListener {

    private GoogleApiClient mGoogleApiClient;
    private View mLayout;
    private Handler mHandler;
    private String nodeId;
    private TextView searchText;
    private GridViewPager mPager;

    private ImageView mSecondIndicator;
    private ImageView mFirstIndicator;
    private ImageAdapter imageAdapter;
    private DelayedConfirmationView delay;
    private String spokenText;
    private RelativeLayout delayLayout;
    private boolean searchIsCanceled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        setContentView(R.layout.activity_photo);
        if(getIntent().getData() != null)
            nodeId = getIntent().getData().getSchemeSpecificPart();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                setupViews();

                mGoogleApiClient = new GoogleApiClient.Builder(PhotoActivity.this)
                        .addApi(Wearable.API)
                        .addConnectionCallbacks(PhotoActivity.this)
                        .addOnConnectionFailedListener(PhotoActivity.this)
                        .build();

                if(mGoogleApiClient != null)
                    mGoogleApiClient.connect();
            }
        });
    }

    private void setupViews()
    {
        mLayout = findViewById(R.id.layout);

        searchText = (TextView)findViewById(R.id.searchText);
        mPager = (GridViewPager) findViewById(R.id.gridPager);
        mFirstIndicator = (ImageView) findViewById(R.id.indicator_0);
        mSecondIndicator = (ImageView) findViewById(R.id.indicator_1);
        delay = (DelayedConfirmationView) findViewById(R.id.delay);
        delayLayout = (RelativeLayout) findViewById(R.id.delayLayout);
        setupConfirmationDelay();

        imageAdapter = new ImageAdapter(PhotoActivity.this, getFragmentManager());
        imageAdapter.setFragmentListener(fragmentListener);
        mPager.setAdapter(imageAdapter);

        setIndicator(0);
        mPager.setOnPageChangeListener(new GridViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int i, int i2, float v, float v2, int i3, int i4) {}

            @Override
            public void onPageSelected(int i, int i2) {
                setIndicator(i2);
            }

            @Override
            public void onPageScrollStateChanged(int i) {}
        });


    }

    private void setupConfirmationDelay()
    {
        delayLayout.setVisibility(View.GONE);
        delay.setTotalTimeMs(2500);

        delay.setListener(new DelayedConfirmationView.DelayedConfirmationListener() {
            @Override
            public void onTimerFinished(View view) {
                if(searchIsCanceled)
                {
                    searchIsCanceled = false;
                    return;
                }
                searchWord(spokenText);
                delayLayout.setVisibility(View.GONE);
            }

            @Override
            public void onTimerSelected(View view) {
                searchIsCanceled = true;
                delayLayout.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient != null) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            Wearable.NodeApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.NodeApi.addListener(mGoogleApiClient, this);


        if (mGoogleApiClient != null)
            Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, "/startImage", null);

    }

    @Override
    public void onConnectionSuspended(int cause) {}

    @Override
    public void onConnectionFailed(ConnectionResult result) {}

    private void generateEvent(final String title, final String text) {}

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();
        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (DataLayerListenerService.IMAGE_PATH.equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    Asset photo = dataMapItem.getDataMap()
                            .getAsset(DataLayerListenerService.IMAGE_KEY);
                    final Bitmap bitmap = loadBitmapFromAsset(mGoogleApiClient, photo);

                    Uri uri = event.getDataItem().getUri();
                    nodeId = uri.getHost();

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            if(mPager.getCurrentItem().x == 1)
                            {
                                mPager.setCurrentItem(0,0, true);
                            }

                            if(imageAdapter.getImageFragment() != null)
                                imageAdapter.getImageFragment().setImage(bitmap);
                        }
                    });

                } else if (DataLayerListenerService.COUNT_PATH.equals(path)) {

                    generateEvent("DataItem Changed", event.getDataItem().toString());
                } else {

                }

            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                generateEvent("DataItem Deleted", event.getDataItem().toString());
            } else {
                generateEvent("Unknown data event type", "Type = " + event.getType());
            }
        }
    }

    /**
     * Extracts {@link android.graphics.Bitmap} data from the
     * {@link com.google.android.gms.wearable.Asset}
     */
    private Bitmap loadBitmapFromAsset(GoogleApiClient apiClient, Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }

        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                apiClient, asset).await().getInputStream();

        if (assetInputStream == null) {
            return null;
        }
        return BitmapFactory.decodeStream(assetInputStream);
    }

    @Override
    public void onMessageReceived(MessageEvent event) {
        generateEvent("Message", event.toString());
    }

    @Override
    public void onPeerConnected(Node node) {
        nodeId = node.getId();generateEvent("Node Connected", node.getId());
    }

    @Override
    public void onPeerDisconnected(Node node) {
        generateEvent("Node Disconnected", node.getId());
    }

    private ImageAdapter.FragmentListener fragmentListener = new ImageAdapter.FragmentListener()
    {

        @Override
        public void OnSearchAction() {
            displaySpeechRecognizer();
        }

        @Override
        public void OnNextImage() {
            if (mGoogleApiClient != null)
                Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, "/next", null);
        }

        @Override
        public void OnPrevImage() {
            if(mGoogleApiClient != null)
                Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, "/prev", null);
        }
    };

    private void setIndicator(int i) {
        switch (i) {
            case 0:
                mFirstIndicator.setImageResource(R.drawable.full_10);
                mSecondIndicator.setImageResource(R.drawable.empty_10);
                break;
            case 1:
                mFirstIndicator.setImageResource(R.drawable.empty_10);
                mSecondIndicator.setImageResource(R.drawable.full_10);
                break;
        }
    }

    private static final int SPEECH_REQUEST_CODE = 0;

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
// Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
// This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            spokenText = results.get(0);
            // Do something with spokenText
            searchText.setText(spokenText);
            delayLayout.setVisibility(View.VISIBLE);
            delay.start();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void searchWord(String word)
    {
        if(mGoogleApiClient != null)
            Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, "/search", word.getBytes());
        spokenText = "";
    }
}
