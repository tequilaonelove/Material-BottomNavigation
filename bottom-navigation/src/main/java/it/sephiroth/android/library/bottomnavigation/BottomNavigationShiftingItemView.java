package it.sephiroth.android.library.bottomnavigation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import it.sephiroth.android.library.bottonnavigation.R;

import static android.util.Log.INFO;
import static it.sephiroth.android.library.bottomnavigation.MiscUtils.log;

/**
 * Created by alessandro on 4/3/16 at 10:55 PM.
 * Project: MaterialBottomNavigation
 */
public class BottomNavigationShiftingItemView extends BottomNavigationItemViewAbstract {
    private static final String TAG = BottomNavigationShiftingItemView.class.getSimpleName();
    private final int paddingTop;
    private final int paddingBottomActive;
    private final int textPaddingTop;
    private final int iconSize;
    private final int paddingBottomInactive;
    private final int textSize;

    private Drawable icon;
    private int centerY;
    private final float maxAlpha;
    private final float minAlpha;
    private final Interpolator interpolator = new DecelerateInterpolator();
    private float textWidth;
    private long animationDuration;
    private final int colorActive;
    private float textX;
    private int textY;

    public BottomNavigationShiftingItemView(final BottomNavigation parent, boolean expanded) {
        super(parent, expanded);

        animationDuration = getResources().getInteger(R.integer.bbn_shifting_item_animation_duration);
        paddingTop = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_item_padding_top);
        paddingBottomActive = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_item_padding_bottom_active);
        paddingBottomInactive = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_item_padding_bottom_inactive);
        iconSize = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_item_icon_size);
        textPaddingTop = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_item_text_padding_top);
        textSize = getResources().getDimensionPixelSize(R.dimen.bbn_shifting_text_size);

        this.colorActive = parent.shiftingItemColorActive;

        log(TAG, Log.INFO, "colorActive: %x", colorActive);

        this.maxAlpha = (float) Color.alpha(colorActive) / 255f;
        this.minAlpha = parent.shiftingItemAlphaInactive;

        log(TAG, Log.VERBOSE, "maxAlpha: %g", this.maxAlpha);
        log(TAG, Log.VERBOSE, "minAlpha: %g", this.minAlpha);

        this.centerY = expanded ? paddingTop : paddingBottomInactive;

        this.textPaint.setHinting(Paint.HINTING_ON);
        this.textPaint.setLinearText(true);
        this.textPaint.setSubpixelText(true);
        this.textPaint.setTextSize(textSize);
        this.textPaint.setColor(colorActive);

        if (!expanded) {
            this.textPaint.setAlpha(0);
        }
    }

    @Override
    protected void onStatusChanged(final boolean expanded, final int size) {
        log(TAG, INFO, "onStatusChanged(%b, %d)", expanded, size);
        final AnimatorSet set = new AnimatorSet();
        set.setDuration(animationDuration * 2);
        set.setInterpolator(interpolator);
        final ValueAnimator animator1 = ValueAnimator.ofInt(getLayoutParams().width, size);
        final ValueAnimator animator2 = ObjectAnimator.ofInt(this, "centerY", expanded ? paddingBottomInactive : paddingTop,
            expanded ? paddingTop : paddingBottomInactive
        );

        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                getLayoutParams().width = (int) animation.getAnimatedValue();

                final float fraction = animation.getAnimatedFraction();

                if (expanded) {
                    icon.setAlpha((int) ((minAlpha + (fraction * (maxAlpha - minAlpha))) * 255));
                    textPaint.setAlpha((int) (((fraction * (maxAlpha))) * 255));
                } else {
                    float alpha = 1.0F - fraction;
                    icon.setAlpha((int) ((minAlpha + (alpha * (maxAlpha - minAlpha))) * 255));
                    textPaint.setAlpha((int) (((alpha * (maxAlpha))) * 255));
                }
            }
        });

        set.playTogether(animator1, animator2);
        set.start();
    }

    private void measureText() {
        log(TAG, INFO, "measureText");
        this.textWidth = textPaint.measureText(getItem().getTitle());
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (null == this.icon) {
            this.icon = getItem().getIcon(getContext());
            icon.setBounds(0, 0, iconSize, iconSize);
            icon.setColorFilter(colorActive, PorterDuff.Mode.SRC_ATOP);
            icon.setAlpha((int) (isExpanded() ? maxAlpha * 255 : minAlpha * 255));
        }

        if (textDirty) {
            measureText();
            textDirty = false;
        }

        if (changed) {
            int w = right - left;
            int h = bottom - top;
            int centerX = (w - iconSize) / 2;
            this.textY = h - paddingBottomActive;
            this.textX = (w - textWidth) / 2;
            icon.setBounds(centerX, centerY, centerX + iconSize, centerY + iconSize);
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        icon.draw(canvas);
        canvas.drawText(
            getItem().getTitle(),
            textX,
            textY,
            textPaint
        );
    }

    @SuppressWarnings ("unused")
    @proguard.annotation.Keep
    public int getCenterY() {
        return centerY;
    }

    @SuppressWarnings ("unused")
    @proguard.annotation.Keep
    public void setCenterY(int value) {
        centerY = value;
        requestLayout();
    }

}
