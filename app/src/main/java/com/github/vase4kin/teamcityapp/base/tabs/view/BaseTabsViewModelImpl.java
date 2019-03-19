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

import android.annotation.SuppressLint;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.github.vase4kin.teamcityapp.R;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseTabsViewModelImpl implements BaseTabsViewModel {

    @BindView(R.id.viewPager)
    protected ViewPager mViewPager;
    @BindView(R.id.tabLayout)
    protected TabLayout mTabLayout;

    private Unbinder mUnbinder;

    protected AppCompatActivity mActivity;
    FragmentAdapter mAdapter;
    private View mView;

    public BaseTabsViewModelImpl(View mView, AppCompatActivity mActivity) {
        this.mView = mView;
        this.mActivity = mActivity;
    }

    @Override
    public void initViews() {
        mUnbinder = ButterKnife.bind(this, mView);
        // Make sure there're no fragments saved in fragment manager (in case the view was reloaded)
        removeAllFragmentsFromFragmentManager();
        mAdapter = new FragmentAdapter(mActivity.getSupportFragmentManager(), mActivity);
        addFragments(mAdapter);
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mViewPager.setAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    /**
     * Remove all fragments from fragment manager
     */
    @SuppressLint("RestrictedApi")
    private void removeAllFragmentsFromFragmentManager() {
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            for (Fragment f : fragments) {
                if (f != null) {
                    ft.remove(f);
                }
            }
            ft.commitNow();
        }
    }

    @Override
    public void unBindViews() {
        mUnbinder.unbind();
    }

    @Override
    public abstract void addFragments(FragmentAdapter fragmentAdapter);

    @Override
    public void updateTabTitle(int tabPosition, String newTitle) {
        String updatedTabTitle = String.format("%s (%s)",
                mAdapter.getFragmentContainers().get(tabPosition).getName(),
                newTitle);
        TabLayout.Tab tab = mTabLayout.getTabAt(tabPosition);
        if (tab != null) {
            tab.setText(updatedTabTitle);
        }
    }
}
