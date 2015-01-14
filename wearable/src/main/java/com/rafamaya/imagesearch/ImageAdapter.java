package com.rafamaya.imagesearch;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by rmaya2 on 1/8/2015.
 */
public class ImageAdapter extends FragmentGridPagerAdapter {

    public interface FragmentListener
    {
        public void OnSearchAction();
        public void OnNextImage();
        public void OnPrevImage();
    };

    final Context mContext;
    private FragmentListener listener;
    private ImageFragment imageFragment;
    private ActionFragment actionFragment;


    public ImageAdapter(final Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getFragment(int row, int col) {
        if(col == 0)
        {
            if(imageFragment == null) {
                imageFragment = ImageFragment.newInstance();
                imageFragment.setNavigationButtonListener(new ImageFragment.NavigationButtonsListener() {

                    @Override
                    public void OnNextClicked() {
                        listener.OnNextImage();
                    }

                    @Override
                    public void OnNextPrevClicked() {
                        listener.OnPrevImage();
                    }
                });
            }
            return imageFragment;
        }else
        {
            if(actionFragment == null) {
                actionFragment = ActionFragment.newInstance();
                actionFragment.setActionButtonListener(new ActionFragment.ActionButtonListener() {
                    @Override
                    public void OnSearchActionClicked() {
                        listener.OnSearchAction();
                    }
                });
            }
            return actionFragment;
        }

    }

    public ImageFragment getImageFragment()
    {
        return imageFragment;
    }

    public ActionFragment getActionFragment()
    {
        return actionFragment;
    }

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount(int row) {
        return 2;
    }

    public void setFragmentListener(FragmentListener listener)
    {
        this.listener = listener;
    }


   public static class ImageFragment extends Fragment{

       public interface NavigationButtonsListener
       {
           public void OnNextClicked();
           public void OnNextPrevClicked();
       };

       private ImageView imageView;
       private Button prev;
       private Button next;
       private LinearLayout layout;
       private NavigationButtonsListener aListener;

       public static ImageFragment newInstance() {
           ImageFragment f = new ImageFragment();
           return f;
       }

       public void setNavigationButtonListener(NavigationButtonsListener listener)
       {
           aListener = listener;
       }

       @Override
       public void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
       }
       @Override
       public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
           View root = inflater.inflate(R.layout.image_fragment, container, false);
           layout = (LinearLayout)root.findViewById(R.id.layout);
           imageView = (ImageView)root.findViewById(R.id.imageView);
           next = (Button)root.findViewById(R.id.next);
           prev = (Button)root.findViewById(R.id.prev);
           setupView();
           return root;
       }

       private void setupView()
       {
               next.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       aListener.OnNextClicked();
                   }
               });

               prev.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       aListener.OnNextPrevClicked();
                   }
               });
       }

       public void setImage(Bitmap bitmap)
       {
           imageView.setImageBitmap(bitmap);
       }
   }

    public static class ActionFragment extends Fragment{

        public interface ActionButtonListener
        {
            public void OnSearchActionClicked();
        };

        private CircledImageView searchAction;
        private LinearLayout layout;
        private ActionButtonListener aListener;

        public static ActionFragment newInstance() {
            ActionFragment f = new ActionFragment();
            return f;
        }

        public void setActionButtonListener(ActionButtonListener listener)
        {
            aListener = listener;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.action_fragment, container, false);
            layout = (LinearLayout)root.findViewById(R.id.layout);
            searchAction = (CircledImageView) root.findViewById(R.id.searchAction);
            setupView();
            return root;
        }

        private void setupView()
        {
            searchAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    aListener.OnSearchActionClicked();
                }
            });
        }
    }
}
