package com.davidparry.widgets;

import android.os.Handler;
import android.os.Message;

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
 */
public class ThreadLoadingHandler extends Handler {

    private WeakReference<ThreadLoadingImageView> weakView;

    public ThreadLoadingHandler(ThreadLoadingImageView view) {
        weakView = new WeakReference<ThreadLoadingImageView>(view);
    }

    @Override
    public void handleMessage(Message msg) {
        ThreadLoadingImageView view = weakView.get();
        if (view != null) {
            view.updateImageFromCache();
        }
    }
}
