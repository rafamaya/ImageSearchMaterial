package com.rafamaya.imagesearch.fragment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.rafamaya.imagesearch.R;
import com.rafamaya.imagesearch.activity.MainSearchActivity;
import com.rafamaya.imagesearch.views.FloatingActionButton;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ImageSearchFragment extends Fragment{

    private FloatingActionButton fabButton;
    private CardView cardView;
    List<String> words = Arrays.asList("cool", "cars", "animals", "cities", "cocktails", "drinks", "foods", "games", "movies", "space objects");
    Set<Integer> visited = new HashSet<Integer>();
    boolean cardHiden = true;

    final static OvershootInterpolator overshootInterpolator = new OvershootInterpolator();
    final static AnticipateInterpolator anticiplerateInterpolator = new AnticipateInterpolator();


    public ImageSearchFragment() {
		// TODO Auto-generated constructor stub
	}
	
	public static Fragment makeFragment()
	{
		ImageSearchFragment fragment = new ImageSearchFragment();
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main_search,
				container, false);

        cardView = (CardView)rootView.findViewById(R.id.card_view);
		
		return rootView;
	}

    @Override
    public void onResume() {
        super.onResume();

        if(fabButton == null) {
            fabButton = new FloatingActionButton.Builder(getActivity())
                    .withDrawable(getResources().getDrawable(android.R.drawable.ic_menu_gallery))
                    .withButtonColor(getResources().getColor(R.color.accent))
                    .withGravity(Gravity.BOTTOM | Gravity.RIGHT)
                    .withMargins(0, 0, 16, 16)
                    .create();
        }

        fabButton.showFloatingActionButton();

        fabButton.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                fabButton.hideFloatingActionButton();
                ((MainSearchActivity)getActivity()).loadLocalImages();
            }

        });

        cardView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideCardView();
                ((MainSearchActivity) getActivity()).search(randomSearch());
            }
        });

        showCardView();

        ((MainSearchActivity)getActivity()).setTitle("ImageSearch");
    }

    @Override
    public void onPause() {
        fabButton.hideFloatingActionButton();
        super.onPause();
    }

    private String randomSearch()
    {
        Random rand = new Random();
        int index = rand.nextInt(words.size());
        if (visited.size() < words.size())
        {
            if(!visited.contains(index))
                visited.add(index);
            else
            {
                while(visited.contains(index) && visited.size() < words.size()) {
                    index = rand.nextInt(words.size());
                    if(!visited.contains(index)) {
                        visited.add(index);
                        break;
                    }
                }
            }
            String randomWord = words.get(index);
            return randomWord;
        }

        return "the end";
    }

    public void hideCardView() {
        if (!cardHiden) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardView, "scaleX", 1, 0);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardView, "scaleY", 1, 0);
            AnimatorSet animSetXY = new AnimatorSet();
            animSetXY.playTogether(scaleX, scaleY);
            animSetXY.setInterpolator(anticiplerateInterpolator);
            animSetXY.setDuration(1000);
            animSetXY.start();
            cardHiden = true;
        }
    }

    public void showCardView() {
        if (cardHiden) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardView, "scaleX", 0, 1);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardView, "scaleY", 0, 1);
            AnimatorSet animSetXY = new AnimatorSet();
            animSetXY.playTogether(scaleX, scaleY);
            animSetXY.setInterpolator(overshootInterpolator);
            animSetXY.setDuration(1000);
            animSetXY.start();
            cardHiden = false;
        }
    }


}
