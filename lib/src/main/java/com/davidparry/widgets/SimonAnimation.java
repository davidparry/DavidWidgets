package com.davidparry.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;

import java.lang.ref.WeakReference;


/**
 * Copyright 2015 David Parry
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * This class only works with sdk versions 16 and above
 */
public class SimonAnimation extends SimonCircle {
    public static final long MINIMAL_RATE = 120;
    protected AnimationListener listener;
    private Paint orignalSectionColor;
    private String[] sectionColors;
    private Paint[] sectionColor;

    public SimonAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadAttributes(attrs, 0);
        init();
    }

    public SimonAnimation(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadAttributes(attrs, defStyleAttr);
        init();
    }

    public void setAnimationListener(AnimationListener listener) {
        this.listener = listener;
    }

    private void loadAttributes(AttributeSet attrs, int defStyle) {
        try {
            final TypedArray parenta = getContext().obtainStyledAttributes(
                    attrs, R.styleable.SimonCircle, defStyle, 0);
            int sections = parenta.getInt(
                    R.styleable.SimonCircle_sc_sections, 4);

            final TypedArray a = getContext().obtainStyledAttributes(
                    attrs, R.styleable.SimonAnimation, defStyle, 0);
            sectionColors = new String[sections];
            String color = a.getString(R.styleable.SimonAnimation_sc_sections_highlight_color);
            if (color != null && color.contains("#")) {
                sectionColors = color.split("\\|");
            }

            a.recycle();
        } catch (Exception e) {
            Log.e(SimonCircle.class.getName(), "Error loading attributes in xml layout", e);
        }
    }

    private void init() {
        if (sectionColors != null && sectionColors.length > 0 && sectionColors[0] != null && sectionColors[0].startsWith("#")) {
            sectionColor = new Paint[sectionColors.length];
            int color = Color.TRANSPARENT;
            for (int i = 0; i < sectionColors.length; i++) {
                color = Color.parseColor(sectionColors[0]);
                // the user could of not supplied the correct amount of colors for Sections use the first one
                if (i < sectionColors.length) {
                    try {
                        color = Color.parseColor(sectionColors[i]);
                    } catch (Exception er) {
                        Log.e(SimonCircle.class.getName(), "Error creating section color ", er);
                    }
                }
                sectionColor[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
                sectionColor[i].setStyle(Paint.Style.FILL);
                sectionColor[i].setColor(color);
            }
        }

    }

    /**
     * @param sequence - the sequence you want to animate
     * @param rate     - the rate in the changes between the changes in millisecnds
     */
    public void animate(int[] sequence, long rate) {
        SimonAnimator animator = new SimonAnimator(this, sequence, rate);
        animator.execute();
    }

    public void postAnimation(RunnableAnimator animator) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            postOnAnimation(animator);
        }
    }

    protected void highlightSection(int i) {
        orignalSectionColor = circleSection.sectionColor[i];
        if (sectionColor != null && sectionColor.length > i) {
            circleSection.sectionColor[i] = sectionColor[i];
        } else {
            circleSection.sectionColor[i] = getBrightColor(circleSection.sectionColor[i]);
        }
    }

    protected void dimSection(int i) {
        circleSection.sectionColor[i] = orignalSectionColor;
    }

    private Paint getBrightColor(Paint paint) {
        Paint _paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _paint.setStyle(Paint.Style.FILL);
        int color = Color.TRANSPARENT;
        int alpha = 255;
        if (paint != null) {
            color = paint.getColor();
            alpha = paint.getAlpha();
        }
        int green = Color.green(color);
        int red = Color.red(color);
        int blue = Color.blue(color);
        if (alpha > 150) {
            alpha = alpha - 150;
        } else if (alpha <= 155) {
            alpha = alpha + 100;
        } else {
            alpha = 100;
        }
        color = Color.argb(alpha, red, green, blue);
        _paint.setColor(color);
        int b = Color.alpha(_paint.getColor());
        return _paint;
    }


    class SimonAnimator extends AsyncTask<Void, Void, String> {

        protected WeakReference<SimonAnimation> weak;
        private int[] sequences;
        private long rate;

        SimonAnimator(SimonAnimation animationCircle, int[] sequences, long rate) {
            this.sequences = sequences;
            this.rate = rate;
            if (this.rate < MINIMAL_RATE) {
                throw new RuntimeException("Rate must be greater then " + MINIMAL_RATE + " milliseconds!");
            }
            weak = new WeakReference<SimonAnimation>(animationCircle);
        }

        @Override
        protected String doInBackground(Void... params) {
            SimonAnimation circle = weak.get();
            if (circle != null) {
                for (int i = 0; i < sequences.length; i++) {
                    circle.postAnimation(new RunnableHighlightAnimator(circle, sequences[i]));
                    try {
                        Thread.sleep(rate);
                    } catch (Exception er) {

                    }
                    circle.postAnimation(new RunnableDimAnimator(circle, sequences[i]));
                }
            }
            return "done";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            SimonAnimation circle = weak.get();
            if (circle != null) {
                circle.listener.stopped();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SimonAnimation circle = weak.get();
            if (circle != null) {
                circle.listener.started();
            }
        }
    }

    abstract class RunnableAnimator implements Runnable {

        protected WeakReference<SimonAnimation> weak;
        protected int section;

        RunnableAnimator(SimonAnimation animationCircle, int section) {
            this.section = section;
            weak = new WeakReference<SimonAnimation>(animationCircle);
        }

    }

    class RunnableHighlightAnimator extends RunnableAnimator {

        RunnableHighlightAnimator(SimonAnimation animationCircle, int section) {
            super(animationCircle, section);
        }

        @Override
        public void run() {
            SimonAnimation circle = weak.get();
            if (circle != null) {
                circle.highlightSection(section);
                circle.invalidate();
            }

        }
    }

    class RunnableDimAnimator extends RunnableAnimator {

        RunnableDimAnimator(SimonAnimation animationCircle, int section) {
            super(animationCircle, section);
        }

        @Override
        public void run() {
            SimonAnimation circle = weak.get();
            if (circle != null) {
                circle.dimSection(section);
                circle.invalidate();
            }
        }
    }
}
