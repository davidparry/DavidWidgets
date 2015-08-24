package com.davidparry.widgets;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import com.davidparry.widgets.util.ImageCache;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
public class ThreadLoadingImageView extends ImageView implements ThreadLoad {
    private static final String TAG = "ThreadLoadingImageView";
    protected static ExecutorService executorService;
    private static ImageCache cache;
    private String url;
    private ThreadLoadingHandler threadHandler;

    public ThreadLoadingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init(attrs, 0);
        }
    }

    public ThreadLoadingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init(attrs, defStyleAttr);
        }
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        if (cache == null) {
            ActivityManager am = (ActivityManager) getContext().getSystemService(
                    Context.ACTIVITY_SERVICE);
            int maxKb = am.getMemoryClass() * 1024;
            int limitKb = maxKb / 6;
            cache = new MemoryCache(limitKb);
        }
        // check to see if the developer is passing in a http link to a image
        loadAttributes(attrs, defStyleAttr);
        threadHandler = new ThreadLoadingHandler(this);
        if (getImageUrl() != null && getImageUrl().length() > 0) {
            loadImageFromUrl(getImageUrl());
        }
    }

    private void prepareForCacheLoading() {
        cache.registerListener(this);
    }

    private void setDrawableImageFromCache() {
        if (getCacheId() != null) {
            Drawable drawable = cache.get(getCacheId());
            if (drawable != null) {
                // no need to call and start loading someone else loaded the image in the cache
                setImageDrawable(drawable);
            } else {
                startLoad();
            }
        }
    }

    private void loadAttributes(AttributeSet attrs, int defStyle) {
        try {
            final TypedArray a = getContext().obtainStyledAttributes(
                    attrs, R.styleable.ThreadLoadingImageView, defStyle, 0);
            this.url = a.getString(
                    R.styleable.ThreadLoadingImageView_image_url);
            a.recycle();
        } catch (Exception e) {
            Log.e(TAG, "Error loading imageUrl from attribute in xml layout", e);
        }
    }

    protected void setUrl(String url) {
        this.url = url;
    }

    protected void updateImageFromCache() {
        setImageDrawable(cache.get(getCacheId()));
        invalidate();
    }

    @Override
    public ImageCache getCache() {
        return this.cache;
    }

    @Override
    public void setCache(ImageCache cache) {
        this.cache = cache;
    }

    @Override
    public void startLoad() {
        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(10);
        }
        executorService.submit(new ImageLoader(this));

    }

    @Override
    public String getImageUrl() {
        return this.url;
    }

    @Override
    public void loadImageFromUrl(String url) {
        // someone is passing a url to load this ImageView
        this.url = url;
        // even if the cache was previously set need to understand url might be new so need to reload potentially
        prepareForCacheLoading();
        setDrawableImageFromCache();

    }

    @Override
    public String getCacheId() {
        // right now using the image url might hash down the road even though map hashes it
        return getImageUrl();
    }

    /**
     * the url for this ImageView has been loaded in the cache either by this View or some outside working Thread
     */
    @Override
    public void loaded() {
        threadHandler.sendMessage(Message.obtain());
    }

    public static class ImageLoader implements Runnable {
        private ThreadLoad loader;

        public ImageLoader(ThreadLoad loader) {
            this.loader = loader;
        }

        public void run() {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(loader.getImageUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                connection.setInstanceFollowRedirects(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                String full = url.getFile();
                int start = full.lastIndexOf("/") + 1;
                if (full.length() > 0) {
                    full = full.substring(start);
                }
                if (input != null) {
                    Drawable image = Drawable.createFromStream(input, full);
                    if (image != null) {
                        if (loader.getCache() != null) {
                            loader.getCache().put(this.loader.getCacheId(), image);
                        }
                    }
                }
            } catch (Exception er) {
                Log.e(TAG, "Error loading Image", er);

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

}
