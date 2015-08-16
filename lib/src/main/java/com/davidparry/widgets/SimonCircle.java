package com.davidparry.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
 */
public class SimonCircle extends ViewGroup implements View.OnTouchListener {
    private CircleSection circleSection;
    private Circles circles;
    private Paint lineColorPaint;
    private OnSectionClickListener listener;
    private int sections = 0;
    private int lineColor = 0;
    private String[] sectionColors;


    public SimonCircle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init(attrs, defStyleAttr);
        }
    }

    public SimonCircle(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(attrs, 0);
        }
    }

    /**
     * Your class that implements the listener to be told when clicks happen on the section
     *
     * @param listener - OnSectionClickListener class to listen for section clicked
     */
    public void setOnSectionClickListener(OnSectionClickListener listener) {
        this.listener = listener;
    }

    /**
     * To go raw to the integer representation be careful with this may change
     * to what the value is per API
     *
     * @param color - the integer value of the color if you wish
     */
    public void setLineColor(int color) {
        this.lineColor = color;
    }

    /**
     * Set the color of the line
     *
     * @param color - hex value for the color
     */
    public void setLineHexColor(String color) {
        try {
            this.lineColor = Color.parseColor(color);
        } catch (Exception er) {
            Log.e(SimonCircle.class.getName(), "Error converting HEX color:" + color, er);
        }
    }

    /**
     * The number of sections in the circle
     *
     * @param amount - the number of sections to draw out 4 is the default
     */
    public void setNumberOfSections(int amount) {
        this.sections = amount;
    }

    /**
     * Set the hex color value for the section per index
     *
     * @param section - start at 0 for the sections passed in
     * @param color   - the hex value for the color at that section
     */
    public void setSectionColor(int section, String color) {
        if (section < sectionColors.length && section >= 0 && color != null && color.contains("#")) {
            sectionColors[section] = color;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // Do nothing. Do not call the superclass method--that would start a layout pass
        // on this view's children.
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        loadAttributes(attrs, defStyleAttr);
        lineColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lineColorPaint.setStyle(Paint.Style.STROKE);
        lineColorPaint.setColor(this.lineColor);
        lineColorPaint.setStrokeWidth(5);
        circleSection = new CircleSection(getContext(), this.sections);
        addView(circleSection);
        this.setOnTouchListener(this);
    }

    private void loadAttributes(AttributeSet attrs, int defStyle) {
        try {
            final TypedArray a = getContext().obtainStyledAttributes(
                    attrs, R.styleable.SimonCircle, defStyle, 0);
            this.sections = a.getInt(
                    R.styleable.SimonCircle_sc_sections, 4);
            this.lineColor = a.getColor(R.styleable.SimonCircle_sc_line_color, Color.TRANSPARENT);
            sectionColors = new String[this.sections];
            String color = a.getString(R.styleable.SimonCircle_sc_sections_color);
            if (color != null && color.contains("#")) {
                sectionColors = color.split("\\|");
            }

            a.recycle();
        } catch (Exception e) {
            Log.e(SimonCircle.class.getName(), "Error loading attributes in xml layout", e);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = Math.max(minw, MeasureSpec.getSize(widthMeasureSpec));
        int minh = getPaddingBottom() + getPaddingTop();
        int h = Math.max(minh, MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(w, h);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float xpad = (float) (getPaddingLeft() + getPaddingRight());
        float ypad = (float) (getPaddingTop() + getPaddingBottom());
        float ww = (float) w - xpad;
        float hh = (float) h - ypad;
        RectF parentBounds = new RectF(
                0.0f,
                0.0f,
                ww,
                hh);
        parentBounds.offsetTo(getPaddingLeft(), getPaddingTop());
        circleSection.layout((int) parentBounds.left,
                (int) parentBounds.top,
                (int) parentBounds.right,
                (int) parentBounds.bottom);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean flag = true;
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            int section = circleSection.sectionMatch(event.getX(), event.getY());
            if (listener != null) {
                flag = listener.onTouch(v, event, section);
            }
        }
        return flag;
    }

    public interface OnSectionClickListener {
        boolean onTouch(View v, MotionEvent event, int sectionClicked);
    }

    private class CircleSection extends View {
        List<SectionPolygon> sectionPolygons;
        private Paint[] sectionColor;
        private int[] degreePoints;
        private Path[] paths;

        public CircleSection(Context context, int sections) {
            super(context);
            init(sections);
        }

        public int sectionMatch(float x, float y) {
            int section = -5;
            for (int i = 0; i < sectionPolygons.size(); i++) {
                if (sectionPolygons.get(i).containsPoint(new Point(x, y))) {
                    section = i;
                    break;
                }
            }
            return section;
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            // Do nothing. Do not call the superclass method--that would start a layout pass
            // on this view's children.
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            circles = new Circles(w, h);
            paths = new Path[degreePoints.length];
            sectionPolygons = new ArrayList<SectionPolygon>();
            for (int i = 0; i < degreePoints.length; i++) {
                int degeePoint = degreePoints[i];
                float[] small = circles.innerCirclePoint(degeePoint);
                float[] big = circles.outerCirclePoint(degeePoint);

                Path path = new Path();
                path.moveTo(small[0], small[1]);
                path.lineTo(big[0], big[1]);
                paths[i] = path;
                SectionPolygon sectionPolygon = new SectionPolygon();
                sectionPolygon.firstLine = new Point[2];
                sectionPolygon.firstLine[0] = new Point();
                sectionPolygon.firstLine[1] = new Point();
                sectionPolygon.firstLine[0] = new Point(small[0], small[1]);
                sectionPolygon.firstLine[1] = new Point(big[0], big[1]);

                int secondDegreePoint = 0;
                if (i == degreePoints.length - 1) {
                    secondDegreePoint = degreePoints[0];
                } else {
                    secondDegreePoint = degreePoints[i + 1];
                }
                float[] smf = circles.innerCirclePoint(secondDegreePoint);
                float[] bmf = circles.outerCirclePoint(secondDegreePoint);

                sectionPolygon.secondLine = new Point[2];
                sectionPolygon.secondLine[0] = new Point();
                sectionPolygon.secondLine[1] = new Point();
                // reverse here to follow the line
                sectionPolygon.secondLine[0] = new Point(bmf[0], bmf[1]);
                sectionPolygon.secondLine[1] = new Point(smf[0], smf[1]);

                List<Point> outerArc = null;
                List<Point> innerArc = null;
                // if we are on the last section
                if (i + 1 == degreePoints.length) {
                    outerArc = sectionPolygon.computeOuterCirclePoints(circles, degeePoint, 360);
                    innerArc = sectionPolygon.computeInnerCirclePoints(circles, degeePoint, 360);
                } else {
                    outerArc = sectionPolygon.computeOuterCirclePoints(circles, degeePoint, secondDegreePoint);
                    innerArc = sectionPolygon.computeInnerCirclePoints(circles, degeePoint, secondDegreePoint);
                }
                if (outerArc != null) {
                    sectionPolygon.outerArc = outerArc.toArray(new Point[outerArc.size()]);
                }
                if (innerArc != null) {
                    sectionPolygon.innerArc = innerArc.toArray(new Point[innerArc.size()]);
                }
                sectionPolygon.makePolygon();
                sectionPolygons.add(sectionPolygon);
            }
        }

        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int i = 0;
            Paint color = lineColorPaint;
            for (SectionPolygon poly : sectionPolygons) {
                if (sectionColor != null && sectionColor.length > 0) {
                    color = sectionColor[i];
                    i++;
                }
                canvas.drawPath(poly.path, color);
            }
            for (SectionPolygon poly : sectionPolygons) {
                canvas.drawPath(poly.path, lineColorPaint);
            }
        }

        private void init(int sections) {
            calculateSections(sections);
            if (sectionColors != null && sectionColors.length > 0) {
                sectionColor = new Paint[sections];
                for (int i = 0; i < sections; i++) {
                    int color = Color.parseColor(sectionColors[0]);
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

        private void calculateSections(int sections) {
            degreePoints = new int[sections];
            int degree = 360 / sections;
            degreePoints[0] = 0;
            int sum = degree;
            for (int i = 1; i < sections; i++) {
                degreePoints[i] = sum;
                sum = sum + degree;
            }
        }
    }

    protected class SectionPolygon {
        protected Point[] points;
        protected Point[] firstLine;
        protected Point[] secondLine;
        protected Point[] outerArc;
        protected Point[] innerArc;
        protected Path path = new Path();
        protected int pointer = 0;

        public void makePolygon() {
            points = new Point[getPointCount()];
            addPoints(firstLine);
            addPoints(outerArc);
            addPoints(secondLine);
            addPoints(innerArc);
            path.moveTo(points[0].x, points[0].y);
            for (int i = 1; i < points.length; i++) {
                path.lineTo(points[i].x, points[i].y);
            }

        }

        public List<Point> computeOuterCirclePoints(SimonCircle.Circles circles, int startAngle, int endAngle) {
            int degree = startAngle + 1;
            List<Point> list = new ArrayList<Point>();
            while (degree < endAngle) {
                float[] xy = circles.outerCirclePoint(degree);
                list.add(new Point(xy[0], xy[1]));
                degree = degree + 1;
            }
            return list;
        }

        public List<Point> computeInnerCirclePoints(SimonCircle.Circles circles, int startAngle, int endAngle) {
            int degree = endAngle - 1;
            List<Point> list = new ArrayList<Point>();
            while (degree > startAngle) {
                float[] xy = circles.innerCirclePoint(degree);
                list.add(new Point(xy[0], xy[1]));
                degree = degree - 1;
            }
            return list;
        }

        private int getPointCount() {
            int v = 0;
            if (firstLine != null) {
                v = firstLine.length;
            }
            if (secondLine != null) {
                v = v + secondLine.length;
            }
            if (outerArc != null) {
                v = v + outerArc.length;
            }
            if (innerArc != null) {
                v = v + innerArc.length;

            }
            return v;
        }

        private void addPoints(Point[] point) {
            if (point != null) {
                for (int i = 0; i < point.length; i++) {
                    points[pointer] = point[i];
                    pointer++;
                }
            }
        }

        public boolean containsPoint(Point test) {
            if (points == null || points.length < 4) {
                makePolygon();
            }
            int i;
            int j;
            boolean result = false;
            for (i = 0, j = points.length - 1; i < points.length; j = i++) {
                if ((points[i].y > test.y) != (points[j].y > test.y) &&
                        (test.x < (points[j].x - points[i].x) * (test.y - points[i].y) / (points[j].y - points[i].y) + points[i].x)) {
                    result = !result;
                }
            }
            return result;
        }

        @Override
        public String toString() {
            return "Polygon{" +
                    "points=" + Arrays.toString(points) +
                    '}';
        }


    }

    protected class Point {
        protected float x;
        protected float y;

        public Point() {
        }

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "Point{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    protected class Circles {
        protected float outerCircleSize;
        protected float innerCircleSize;
        protected float x;
        protected float y;

        protected Circles(int w, int h) {
            RectF bounds = new RectF(0, 0, w, h);
            float diameter = Math.min(w, h);
            this.x = bounds.centerX();
            this.y = bounds.centerY();
            innerCircleSize = diameter / 10;
            outerCircleSize = diameter / 2.2f;
        }

        protected float[] outerCirclePoint(float angleDeg) {
            return xyForAngle(angleDeg, outerCircleSize);
        }

        protected float[] innerCirclePoint(float angleDeg) {
            return xyForAngle(angleDeg, innerCircleSize);
        }

        private float[] xyForAngle(float angleDeg, float circleSize) {
            float values[] = new float[2];
            float xPos = this.x + (circleSize * (float) Math.cos(Math.toRadians(angleDeg)));
            float yPos = this.y + (circleSize * (float) Math.sin(Math.toRadians(angleDeg)));
            values[0] = xPos;
            values[1] = yPos;
            return values;
        }

    }


}
