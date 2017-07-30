package news.androidtv.launchonboot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.felkertech.settingsmanager.SettingsManager;

import java.util.ArrayList;

/**
 * Created by Nick on 4/22/2017.
 */

public class OnboardingFragment extends android.support.v17.leanback.app.OnboardingFragment {
    private static final int[] pageTitles = {
            R.string.onboarding_title_welcome,
            R.string.onboarding_title_what,
            R.string.onboarding_title_tv,
            R.string.onboarding_title_try
    };
    private static final int[] pageDescriptions = {
            R.string.onboarding_description_welcome,
            R.string.onboarding_description_what,
            R.string.onboarding_description_tv,
            R.string.onboarding_description_try
    };
    private final int[] pageImages = {
            R.drawable.tv_animation_d,
            R.drawable.tv_animation_a,
            R.drawable.tv_animation_b,
            R.drawable.tv_animation_c
    };
    private static final long ANIMATION_DURATION = 500;
    private Animator mContentAnimator;
    private ImageView mContentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Set the logo to display a splash animation
        setLogoResourceId(R.drawable.banner);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void onFinishFragment() {
        super.onFinishFragment();
        // Our onboarding is done
        // Let's go back to the MainActivity
        getActivity().finish();
    }

    @Override
    protected int getPageCount() {
        return pageTitles.length;
    }

    @Override
    protected String getPageTitle(int pageIndex) {
        return getString(pageTitles[pageIndex]);
    }

    @Override
    protected String getPageDescription(int pageIndex) {
        return getString(pageDescriptions[pageIndex]);
    }

    @Nullable
    @Override
    protected View onCreateBackgroundView(LayoutInflater inflater, ViewGroup container) {
        View bgView = new View(getActivity());
        bgView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        return bgView;
    }

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, ViewGroup container) {
        mContentView = new ImageView(getActivity());
        mContentView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mContentView.setPadding(0, 32, 0, 32);
        return mContentView;
    }

    @Nullable
    @Override
    protected View onCreateForegroundView(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    @Override
    protected void onPageChanged(final int newPage, int previousPage) {
        if (mContentAnimator != null) {
            mContentAnimator.end();
        }
        ArrayList<Animator> animators = new ArrayList<>();
        Animator fadeOut = createFadeOutAnimator(mContentView);

        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mContentView.setImageDrawable(getResources().getDrawable(pageImages[newPage]));
                ((AnimationDrawable) mContentView.getDrawable()).start();
            }
        });
        animators.add(fadeOut);
        animators.add(createFadeInAnimator(mContentView));
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(animators);
        set.start();
        mContentAnimator = set;
    }
    @Override
    protected Animator onCreateEnterAnimation() {
        mContentView.setImageDrawable(getResources().getDrawable(pageImages[0]));
        ((AnimationDrawable) mContentView.getDrawable()).start();
        mContentAnimator = createFadeInAnimator(mContentView);
        return mContentAnimator;
    }
    private Animator createFadeInAnimator(View view) {
        return ObjectAnimator.ofFloat(view, View.ALPHA, 0.0f, 1.0f).setDuration(ANIMATION_DURATION);
    }

    private Animator createFadeOutAnimator(View view) {
        return ObjectAnimator.ofFloat(view, View.ALPHA, 1.0f, 0.0f).setDuration(ANIMATION_DURATION);
    }
}
