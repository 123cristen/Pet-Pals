package com.petpals;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Sunnie on 4/27/2017.
 *
 * disclaimer: I do not own this font. This font is from http://www.1001fonts.com/disposabledroid-bb-font.html
 */
public class PixelTextView extends android.support.v7.widget.AppCompatTextView {
    public PixelTextView(Context context) {
        super(context);
        init();
    }

    public PixelTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PixelTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){}{
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/disposabledroid-bb.bold.ttf");
            setTypeface(tf);
        }
    }

}
