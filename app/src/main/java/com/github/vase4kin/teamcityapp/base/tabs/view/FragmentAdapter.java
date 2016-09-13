/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.base.tabs.view;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation of FragmentPagerAdapter
 */
public class FragmentAdapter extends FragmentPagerAdapter {

    private List<FragmentContainer> fragmentContainers = new ArrayList<>();
    private Activity mActivity;

    public FragmentAdapter(FragmentManager fm, Activity activity) {
        super(fm);
        this.mActivity = activity;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentContainers.get(position).getFragment();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentContainers.get(position).getName();
    }

    @Override
    public int getCount() {
        return fragmentContainers.size();
    }

    public void add(int name, Fragment fragment) {
        fragmentContainers.add(new FragmentContainer(name, fragment));
    }

    public List<FragmentContainer> getFragmentContainers() {
        return fragmentContainers;
    }

    /**
     * Container for fragment
     */
    public class FragmentContainer {

        private int name;
        private Fragment fragment;

        public FragmentContainer(int name, Fragment fragment) {
            this.name = name;
            this.fragment = fragment;
        }

        public String getName() {
            return mActivity.getString(name);
        }

        public Fragment getFragment() {
            return fragment;
        }
    }
}
