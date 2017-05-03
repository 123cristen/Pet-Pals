package com.petpals;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by Sunnie on 5/3/2017.
 */

public class PixelButton extends android.support.v7.widget.AppCompatButton {
    public PixelButton(Context context) {
        super(context);
        init();
    }

    public PixelButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PixelButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){}{
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/disposabledroid-bb.bold.ttf");
            setTypeface(tf);
            setTextSize(20f);
        }
    }
}
