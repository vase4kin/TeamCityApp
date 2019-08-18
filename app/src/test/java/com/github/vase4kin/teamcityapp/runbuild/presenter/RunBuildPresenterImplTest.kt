/*
 * Copyright 2019 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.runbuild.presenter

import com.github.vase4kin.teamcityapp.account.create.data.OnLoadingListener
import com.github.vase4kin.teamcityapp.agents.api.Agent
import com.github.vase4kin.teamcityapp.properties.api.Properties
import com.github.vase4kin.teamcityapp.runbuild.interactor.BranchesInteractor
import com.github.vase4kin.teamcityapp.runbuild.interactor.LoadingListenerWithForbiddenSupport
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor
import com.github.vase4kin.teamcityapp.runbuild.router.RunBuildRouter
import com.github.vase4kin.teamcityapp.runbuild.tracker.RunBuildTracker
import com.github.vase4kin.teamcityapp.runbuild.view.BranchesComponentView
import com.github.vase4kin.teamcityapp.runbuild.view.RunBuildView
import com.github.vase4kin.teamcityapp.utils.capture
import com.github.vase4kin.teamcityapp.utils.eq
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.runners.MockitoJUnitRunner
import java.util.*

/**
 * Tests for [RunBuildPresenterImpl]
 */
@RunWith(MockitoJUnitRunner::class)
class RunBuildPresenterImplTest {

    @Captor
    private lateinit var propertiesArgumentCaptor: ArgumentCaptor<Properties>
    @Captor
    private lateinit var queueLoadingListenerCaptor: ArgumentCaptor<LoadingListenerWithForbiddenSupport<String>>
    @Captor
    private lateinit var branchLoadingListenerCaptor: ArgumentCaptor<OnLoadingListener<List<String>>>
    @Captor
    private lateinit var agentsLoadingListenerCaptor: ArgumentCaptor<OnLoadingListener<List<Agent>>>
    @Mock
    private lateinit var view: RunBuildView
    @Mock
    private lateinit var interactor: RunBuildInteractor
    @Mock
    private lateinit var router: RunBuildRouter
    @Mock
    private lateinit var tracker: RunBuildTracker
    @Mock
    private lateinit var branchesComponentView: BranchesComponentView
    @Mock
    private lateinit var branchesInteractor: BranchesInteractor
    private val agent = Agent("agentName")
    private lateinit var presenter: RunBuildPresenterImpl

    @Before
    fun setUp() {
        presenter = RunBuildPresenterImpl(view, interactor, router, tracker, branchesComponentView, branchesInteractor)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(view, interactor, router, tracker)
    }

    @Test
    fun testOnCreate() {
        presenter.onCreate()
        verify(view).initViews(eq(presenter))
        verify(branchesInteractor).loadBranches(capture(branchLoadingListenerCaptor))
        val loadingListener = branchLoadingListenerCaptor.value
        val singleBranchList = listOf("Branch")
        loadingListener.onSuccess(singleBranchList)
        verify(branchesComponentView).hideBranchesLoadingProgress()
        verify(branchesComponentView).setupAutoCompleteForSingleBranch(eq(singleBranchList))
        verify(branchesComponentView).showBranchesAutoComplete()
        val multipleBranchList = ArrayList<String>()
        multipleBranchList.add("branch1")
        multipleBranchList.add("branch2")
        loadingListener.onSuccess(multipleBranchList)
        verify(branchesComponentView, times(2)).hideBranchesLoadingProgress()
        verify(branchesComponentView).setupAutoComplete(eq<List<String>>(multipleBranchList))
        verify(branchesComponentView, times(2)).showBranchesAutoComplete()
        loadingListener.onFail("")
        verify(branchesComponentView, times(3)).hideBranchesLoadingProgress()
        verify(branchesComponentView).showNoBranchesAvailable()
        verify(view).disableAgentSelectionControl()
        verify(interactor).loadAgents(capture(agentsLoadingListenerCaptor))
        val agentListOnLoadingListener = agentsLoadingListenerCaptor.value
        agentListOnLoadingListener.onFail("fail")
        assertThat(presenter.mAgents.isEmpty(), `is`(equalTo(true)))
        verify(view).hideLoadingAgentsProgress()
        verify(view).showNoAgentsAvailable()
        agentListOnLoadingListener.onSuccess(listOf(agent))
        assertThat(presenter.mAgents.size, `is`(equalTo(1)))
        assertThat(presenter.mAgents[0], `is`(equalTo(agent)))
        verify(view, times(2)).hideLoadingAgentsProgress()
        verify(view).showSelectedAgentView()
        verify(view).enableAgentSelectionControl()
        verify(view).setAgentListDialogWithAgentsList(listOf("agentName"))
        agentListOnLoadingListener.onSuccess(emptyList())
        assertThat(presenter.mAgents.isEmpty(), `is`(equalTo(true)))
        verify(view, times(3)).hideLoadingAgentsProgress()
        verify(view, times(2)).showNoAgentsAvailable()
    }

    @Test
    fun testOnDestroy() {
        presenter.onDestroy()
        verify(interactor).unsubscribe()
        verify(branchesInteractor).unsubscribe()
        verify(view).unbindViews()
    }

    @Test
    fun testOnBuildQueue() {
        presenter.mSelectedAgent = agent
        presenter.mProperties.add(Properties.Property(PROPERTY_NAME, PROPERTY_VALUE))
        `when`(branchesComponentView.branchName).thenReturn("branch")
        presenter.onBuildQueue(true, true, true)
        verify(branchesComponentView).branchName
        verify(view).showQueuingBuildProgress()
        verify(interactor).queueBuild(eq("branch"), eq(agent), eq(true), eq(true), eq(true), capture(propertiesArgumentCaptor), capture(queueLoadingListenerCaptor))
        val capturedProperties = propertiesArgumentCaptor.value
        assertThat(capturedProperties.objects.size, `is`(equalTo(1)))
        val capturedProperty = capturedProperties.objects[0]
        assertThat(capturedProperty.name, `is`(equalTo(PROPERTY_NAME)))
        assertThat(capturedProperty.value, `is`(equalTo(PROPERTY_VALUE)))
        val loadingListener = queueLoadingListenerCaptor.value
        loadingListener.onSuccess("href")
        verify(view).hideQueuingBuildProgress()
        verify(router).closeOnSuccess(eq("href"))
        verify(tracker).trackUserRunBuildWithCustomParamsSuccess()
        presenter.mProperties.clear()
        loadingListener.onSuccess("href")
        verify(view, times(2)).hideQueuingBuildProgress()
        verify(router, times(2)).closeOnSuccess(eq("href"))
        verify(tracker).trackUserRunBuildSuccess()
        loadingListener.onFail("")
        verify(view, times(3)).hideQueuingBuildProgress()
        verify(view).showErrorSnackbar()
        verify(tracker).trackUserRunBuildFailed()
        loadingListener.onForbiddenError()
        verify(view, times(4)).hideQueuingBuildProgress()
        verify(view).showForbiddenErrorSnackbar()
        verify(tracker).trackUserRunBuildFailedForbidden()
    }

    @Test
    fun testOnClick() {
        presenter.onClick()
        verify(router).closeOnCancel()
    }

    @Test
    fun testOnBackPressed() {
        presenter.onBackPressed()
        verify(router).closeOnBackButtonPressed()
    }

    @Test
    fun testOnResume() {
        presenter.onResume()
        verify(tracker).trackView()
    }

    @Test
    fun testOnAgentSelectedIfAgentsAreEmpty() {
        presenter.mAgents = emptyList()
        presenter.onAgentSelected(0)
    }

    @Test
    fun testOnAgentSelectedIfAgentsAreNotEmpty() {
        presenter.mAgents = listOf(agent)
        presenter.onAgentSelected(0)
        assertThat(presenter.mSelectedAgent, `is`(equalTo(agent)))
    }

    @Test
    fun testOnAddParameterButtonClick() {
        presenter.onAddParameterButtonClick()
        verify(view).showAddParameterDialog()
        verify(tracker).trackUserClicksOnAddNewBuildParamButton()
    }

    @Test
    fun testOnClearAllParametersButtonClick() {
        presenter.onClearAllParametersButtonClick()
        assertThat(presenter.mProperties.size, `is`(equalTo(0)))
        verify(view).disableClearAllParametersButton()
        verify(view).showNoneParametersView()
        verify(view).removeAllParameterViews()
        verify(tracker).trackUserClicksOnClearAllBuildParamsButton()
    }

    @Test
    fun testOnParameterAdded() {
        presenter.onParameterAdded(PROPERTY_NAME, PROPERTY_VALUE)
        assertThat(presenter.mProperties.size, `is`(equalTo(1)))
        assertThat(presenter.mProperties[0].name, `is`(equalTo(PROPERTY_NAME)))
        assertThat(presenter.mProperties[0].value, `is`(equalTo(PROPERTY_VALUE)))
        verify(view).hideNoneParametersView()
        verify(view).enableClearAllParametersButton()
        verify(view).addParameterView(eq(PROPERTY_NAME), eq(PROPERTY_VALUE))
        verify(tracker).trackUserAddsBuildParam()
    }

    companion object {

        private val PROPERTY_NAME = "name"
        private val PROPERTY_VALUE = "value"
    }

}