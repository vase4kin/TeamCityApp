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

package com.github.vase4kin.teamcityapp.overview.data;

import android.content.Context;
import android.support.annotation.NonNull;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.base.list.data.BaseListRxDataManagerImpl;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlist.api.CanceledInfo;
import com.github.vase4kin.teamcityapp.buildlist.api.Triggered;
import com.github.vase4kin.teamcityapp.buildlist.api.User;
import com.github.vase4kin.teamcityapp.buildtabs.data.OnOverviewRefreshDataEvent;
import com.github.vase4kin.teamcityapp.navigation.api.BuildElement;
import com.github.vase4kin.teamcityapp.utils.DateUtils;
import com.github.vase4kin.teamcityapp.utils.IconUtils;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Impl of {@link OverViewInteractor}
 */
public class OverviewInteractorImpl extends BaseListRxDataManagerImpl<Build, BuildElement> implements OverViewInteractor {

    private static final String TIME_ICON = "{mdi-clock}";
    private static final String BRANCH_ICON = "{mdi-git}";
    private static final String AGENT_ICON = "{md-directions-railway}";
    private static final String TRIGGER_BY_ICON = "{md-account-circle}";

    private TeamCityService mTeamCityService;
    private Context mContext;
    private EventBus mEventBus;
    private OnOverviewEventsListener mListener;

    public OverviewInteractorImpl(TeamCityService teamCityService,
                                  Context context,
                                  EventBus eventBus) {
        this.mTeamCityService = teamCityService;
        this.mContext = context;
        this.mEventBus = eventBus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setListener(OnOverviewEventsListener listener) {
        this.mListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(@NonNull String url, @NonNull final OnLoadingListener<List<BuildElement>> loadingListener) {
        mSubscriptions.clear();
        Subscription subscription = mTeamCityService.build(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Build>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadingListener.onFail(e.getMessage());
                    }

                    @Override
                    public void onNext(Build response) {
                        loadingListener.onSuccess(createElementsList(response));
                    }
                });
        mSubscriptions.add(subscription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postStopBuildEvent() {
        mEventBus.post(new StopBuildEvent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribeToEventBusEvents() {
        mEventBus.register(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubsribeFromEventBusEvents() {
        mEventBus.unregister(this);
    }

    /***
     * Handle receiving post events from {@link EventBus}
     *
     * @param event {@link OnOverviewRefreshDataEvent}
     */
    @SuppressWarnings("unused")
    public void onEvent(OnOverviewRefreshDataEvent event) {
        if (mListener == null) return;
        mListener.onDataRefreshEvent();
    }

    /**
     * Create list of build elements
     *
     * Can be not the proper place to have this method
     *
     * TODO: Move to presenter
     */
    private List<BuildElement> createElementsList(@NonNull Build build) {
        List<BuildElement> elements = new ArrayList<>();
        if (build.isQueued()) {
            if (build.getWaitReason() != null) {
                elements.add(new BuildElement(IconUtils.getBuildStatusIcon(build.getStatus(), build.getState()),
                        build.getStatusText(),
                        mContext.getString(R.string.build_wait_reason_section_text)));
            }
        } else {
            elements.add(new BuildElement(IconUtils.getBuildStatusIcon(build.getStatus(), build.getState()),
                    build.getStatusText(),
                    mContext.getString(R.string.build_result_section_text)));
        }
        if (build.getCanceledInfo() != null) {
            CanceledInfo canceledInfo = build.getCanceledInfo();
            String userName = null;
            if (canceledInfo.getUser() != null) {
                User user = canceledInfo.getUser();
                if (user.getName() != null) {
                    userName = user.getName();
                } else {
                    userName = user.getUsername();
                }
            }
            if (userName != null) {
                elements.add(
                        new BuildElement(IconUtils.getBuildStatusIcon(build.getStatus(), build.getState()),
                                userName,
                                mContext.getString(R.string.build_canceled_by_text)));
            }
            elements.add(new BuildElement(
                    TIME_ICON,
                    DateUtils.initWithDate(canceledInfo.getTimestamp()).formatStartDateToBuildTitle(),
                    mContext.getString(R.string.build_cancellation_time_text)));
        }
        BuildElement timeBuildElement;
        if (build.isRunning()) {
            timeBuildElement = new BuildElement(TIME_ICON,
                    DateUtils.initWithDate(build.getStartDate()).formatStartDateToBuildTitle(), mContext.getString(R.string.build_time_section_text));
        } else if (build.isQueued()) {
            timeBuildElement = new BuildElement(TIME_ICON,
                    DateUtils.initWithDate(build.getQueuedDate()).formatStartDateToBuildTitle(), mContext.getString(R.string.build_queued_time_section_text));
        } else {
            timeBuildElement = new BuildElement(TIME_ICON,
                    DateUtils.initWithDate(build.getStartDate(), build.getFinishDate()).formatDateToOverview(), mContext.getString(R.string.build_time_section_text));
        }
        elements.add(timeBuildElement);
        if (build.isQueued() && build.getStartEstimate() != null) {
            BuildElement estimatedTimeBuildElement = new BuildElement(TIME_ICON,
                    DateUtils.initWithDate(build.getStartEstimate()).formatStartDateToBuildTitle(), mContext.getString(R.string.build_time_to_start_section_text));
            elements.add(estimatedTimeBuildElement);
        }
        if (build.getBranchName() != null) {
            elements.add(new BuildElement(BRANCH_ICON, build.getBranchName(), mContext.getString(R.string.build_branch_section_text)));
        }
        if (build.getAgent() != null) {
            elements.add(new BuildElement(AGENT_ICON, build.getAgent().getName(), mContext.getString(R.string.build_agent_section_text)));
        }
        String triggeredBy;
        Triggered triggered = build.getTriggered();
        if (triggered.isVcs()) {
            // TODO: change icon to git
            triggeredBy = build.getTriggered().getDetails();
        } else if (triggered.isUser()) {
            if (triggered.getUser() == null) {
                triggeredBy = mContext.getString(R.string.triggered_deleted_user_text);
            } else {
                triggeredBy = triggered.getUser().getName() == null
                        ? build.getTriggered().getUser().getUsername()
                        : build.getTriggered().getUser().getName();
            }
        } else if (triggered.isUnknown()) {
            // TODO: Change icon for schedule
            triggeredBy = build.getTriggered().getDetails();
        } else if (triggered.isBuildType()) {
            // TODO: Get buildType call to get triggered by
            // TODO: get the userName of triggered configuration
            // TODO: navigate by clicking to triggered build
            // TODO: move to resources
            if (build.getTriggered().getBuildType() == null) {
                triggeredBy = "Deleted configuration";
            } else {
                triggeredBy = build.getTriggered().getBuildType().getProjectName() + " " + build.getTriggered().getBuildType().getName();
            }
        } else {
            triggeredBy = mContext.getString(R.string.unknown_trigger_type_text);
        }
        elements.add(new BuildElement(TRIGGER_BY_ICON, triggeredBy, mContext.getString(R.string.build_triggered_by_section_text)));
        return elements;
    }
}
