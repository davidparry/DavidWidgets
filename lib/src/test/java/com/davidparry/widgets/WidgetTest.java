package com.davidparry.widgets;

import android.app.Activity;
import android.content.res.Resources;
import android.util.AttributeSet;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

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
@RunWith(WidgetRoboletricRunner.class)
@Config(constants = BuildConfig.class,sdk = 21)
public class WidgetTest {
    private Resources mResources;

    @Before
    public void setUp() {
        mResources = Resources.getSystem();
    }
    @Test
    public void initTest() throws Exception {
        Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        AttributeSet attr = Mockito.mock(AttributeSet.class);
        ThreadLoadingImageView view = new ThreadLoadingImageView(activity,attr);
        Assert.assertNotNull(view);
        view.setUrl("http://www.davidparry.com/storage/profilesiloet50.png");
    }
}
