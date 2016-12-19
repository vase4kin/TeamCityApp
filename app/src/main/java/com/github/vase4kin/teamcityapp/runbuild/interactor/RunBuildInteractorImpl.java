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

package com.github.vase4kin.teamcityapp.runbuild.interactor;

import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.runbuild.api.Branch;
import com.github.vase4kin.teamcityapp.runbuild.api.Branches;

import java.util.List;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Impl of {@link RunBuildInteractor}
 */
public class RunBuildInteractorImpl implements RunBuildInteractor {

    /**
     * TeamCity Rest Api instance
     */
    private TeamCityService mTeamCityService;
    /**
     * Build type id
     */
    @NonNull
    private String mBuildTypeId;
    /**
     * To handle rx subscriptions
     */
    private CompositeSubscription mSubscription = new CompositeSubscription();

    public RunBuildInteractorImpl(TeamCityService teamCityService, @NonNull String buildTypeId) {
        this.mTeamCityService = teamCityService;
        this.mBuildTypeId = buildTypeId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadBranches(final OnLoadingListener<List<String>> loadingListener) {
        Subscription loadBranchesSubsription = mTeamCityService.listBranches(mBuildTypeId)
                .flatMap(new Func1<Branches, Observable<Branch>>() {
                    @Override
                    public Observable<Branch> call(Branches branches) {
                        return Observable.from(branches.getBranches());
                    }
                })
                .flatMap(new Func1<Branch, Observable<Branch>>() {
                    @Override
                    public Observable<Branch> call(Branch branch) {
                        return Observable.just(branch);
                    }
                })
                .flatMap(new Func1<Branch, Observable<String>>() {
                    @Override
                    public Observable<String> call(Branch branch) {
                        return Observable.just(branch.getName());
                    }
                })
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<String>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadingListener.onFail(e.getMessage());
                    }

                    @Override
                    public void onNext(List<String> branches) {
                        loadingListener.onSuccess(branches);
                    }
                });
        mSubscription.add(loadBranchesSubsription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void queueBuild(String branchName, final LoadingListenerWithForbiddenSupport<String> loadingListener) {
        Build build = new Build();
        build.setBranchName(branchName);
        build.setBuildTypeId(mBuildTypeId);
        Subscription queueBuildSubscription = mTeamCityService.queueBuild(build)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Build>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof HttpException) {
                            HttpException exception = (HttpException) e;
                            if (exception.code() == CODE_FORBIDDEN) {
                                loadingListener.onForbiddenError();
                            } else {
                                loadingListener.onFail(e.getMessage());
                            }
                        } else {
                            loadingListener.onFail(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(Build build) {
                        loadingListener.onSuccess(build.getHref());
                    }
                });
        mSubscription.add(queueBuildSubscription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe() {
        mSubscription.unsubscribe();
    }
}
