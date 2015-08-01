package com.davidparry.widgets;

import android.graphics.drawable.Drawable;

import java.lang.ref.SoftReference;
import java.util.*;

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
public class MemoryCache {

    private static Map<String, SoftReference<Drawable>> cache = Collections.synchronizedMap(new HashMap<String, SoftReference<Drawable>>());
    private static Map<String, List<Listener>> listeners = new HashMap<>();

    public void registerListener(Listener listener) {
        addListener(listener);
    }

    public Drawable get(String id) {
        if (!cache.containsKey(id))
            return null;
        SoftReference<Drawable> ref = cache.get(id);
        return ref.get();
    }

    public void put(String id, Drawable bitmap) {
        cache.put(id, new SoftReference<>(bitmap));
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
        cache.clear();
    }

    public interface Listener {

        void loaded();

        String getCacheId();
    }
}
