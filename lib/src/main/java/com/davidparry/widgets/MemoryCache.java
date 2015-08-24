package com.davidparry.widgets;

import android.graphics.drawable.Drawable;
import com.davidparry.widgets.util.ImageCache;
import com.davidparry.widgets.util.ImageLruCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class MemoryCache implements ImageCache {

    private static Map<String, List<Listener>> listeners = new HashMap<>();
    private ImageLruCache cache;

    public MemoryCache(int size) {
        if (size < 0) {
            size = 1024;
        }
        cache = new ImageLruCache(size);
    }

    @Override
    public void registerListener(Listener listener) {
        addListener(listener);
    }

    @Override
    public Drawable get(String id) {
        return cache.get(id);
    }

    public void put(String id, Drawable bitmap) {
        cache.put(id, bitmap);
        notifyListeners(id);
    }

    private void notifyListeners(String id) {
        List<Listener> list = listeners.get(id);
        if (list != null) {
            for (Listener ref : list) {
                if (ref != null) {
                    ref.loaded();
                }
            }
        }
    }

    private void addListener(Listener listener) {
        if (listener != null) {
            List<Listener> items = listeners.get(listener.getCacheId());
            if (items == null) {
                listeners.put(listener.getCacheId(), new ArrayList<Listener>());
                items = listeners.get(listener.getCacheId());
            }
            items.add(listener);
            listeners.put(listener.getCacheId(), items);
        }
    }


    public void clear() {
        cache.evictAll();
    }

    public interface Listener {

        void loaded();

        String getCacheId();
    }
}
