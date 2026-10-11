/*
 * Copyright 2026 Andrey Tolpeev
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

package teamcityapp.features.build_overview.impl

import org.junit.Assert.*
import org.junit.Test
import teamcityapp.libraries.builds.*

class OverviewRowsTest {
    private fun rows(scene: String) = overviewRows(BuildOverviewFixtures.build(scene))
    private fun row(scene: String, field: OverviewField) = rows(scene).single { it.field == field }

    @Test fun finishedRowsKeepTheirExactOrderAndLegacyDurationSuffix() {
        assertEquals(listOf(OverviewField.Result, OverviewField.Time, OverviewField.Branch, OverviewField.Agent, OverviewField.TriggeredBy, OverviewField.Configuration, OverviewField.Project), rows("finished").map { it.field })
        assertEquals(OverviewText.Literal("21 Jun 16 23:00 - 23:30 (30m:)"), row("finished", OverviewField.Time).value)
    }

    @Test fun runningUsesStartTimeAndRunningIndicator() {
        assertEquals(OverviewIcon.Running, row("running", OverviewField.Result).icon)
        assertEquals(OverviewText.Literal("21 Jun 16 23:00"), row("running", OverviewField.Time).value)
    }

    @Test fun queuedUsesWaitReasonQueuedTimeAndEstimate() {
        assertEquals(listOf(OverviewField.WaitReason, OverviewField.QueuedTime, OverviewField.EstimatedTime), rows("queued").take(3).map { it.field })
        assertEquals(OverviewText.Literal("Waiting for an available agent"), row("queued", OverviewField.WaitReason).value)
    }

    @Test fun queuedMissingReasonHasTheLegacyFallbackAndNoEstimate() {
        assertEquals(OverviewText.QueuedBuild, row("queued_fallback", OverviewField.WaitReason).value)
        assertEquals(R.string.overview_wait_reason, row("queued_fallback", OverviewField.WaitReason).labelRes)
        assertEquals(teamcityapp.libraries.theme.UiText.Resource(R.string.overview_queued_build), row("queued_fallback", OverviewField.WaitReason).text)
        assertFalse(rows("queued_fallback").any { it.field == OverviewField.EstimatedTime })
    }

    @Test fun cancellationInfoComesBeforeTime() {
        assertEquals(listOf(OverviewField.Result, OverviewField.CancelledBy, OverviewField.CancellationTime, OverviewField.Time), rows("cancelled").take(4).map { it.field })
        assertEquals(OverviewText.Literal("John"), row("cancelled", OverviewField.CancelledBy).value)
    }

    @Test fun cancellationWithoutAUserOmitsOnlyCancelledBy() {
        assertFalse(rows("cancelled_without_user").any { it.field == OverviewField.CancelledBy })
        assertEquals(OverviewText.Literal("21 Jun 16 23:10"), row("cancelled_without_user", OverviewField.CancellationTime).value)
    }

    @Test fun userNameWinsAndRestartFallsBackToUsername() {
        assertEquals(OverviewText.Literal("John"), row("user_trigger", OverviewField.TriggeredBy).value)
        assertEquals(OverviewText.Literal("john"), row("restarted", OverviewField.RestartedBy).value)
    }

    @Test fun deletedUserAndConfigurationHaveExplicitFallbacks() {
        assertEquals(OverviewText.DeletedUser, row("deleted_user", OverviewField.TriggeredBy).value)
        assertEquals(teamcityapp.libraries.theme.UiText.Resource(R.string.overview_deleted_user), row("deleted_user", OverviewField.TriggeredBy).text)
        assertEquals(OverviewText.DeletedConfiguration, row("deleted_configuration", OverviewField.TriggeredBy).value)
    }

    @Test fun configurationTriggerIncludesProjectAndName() {
        assertEquals(OverviewText.Literal("Parent project Release"), row("configuration_trigger", OverviewField.TriggeredBy).value)
    }

    @Test fun unknownTriggerIsDistinctFromKnownTriggerWithMissingDetails() {
        assertEquals(OverviewText.UnknownTrigger, row("unknown_trigger", OverviewField.TriggeredBy).value)
        assertFalse(rows("missing_trigger_details").any { it.field == OverviewField.TriggeredBy })
    }

    @Test fun personalBuildHasItsOwnUserRow() {
        assertEquals(OverviewText.Literal("John"), row("personal", OverviewField.Personal).value)
    }

    @Test fun missingOptionalSectionsAreOmitted() {
        assertEquals(listOf(OverviewField.Result, OverviewField.Time, OverviewField.TriggeredBy), rows("minimal").map { it.field })
    }

    @Test fun missingProjectNameHidesBothConfigurationRows() {
        val build = BuildOverviewFixtures.finished.copy(configuration = BuildConfigurationData("bt", "Build", "p", null))
        assertFalse(overviewRows(build).any { it.field == OverviewField.Configuration || it.field == OverviewField.Project })
    }

    @Test fun rowActionTypesDoNotDependOnLocalizedHeaders() {
        assertEquals(OverviewRowAction.Branch, row("finished", OverviewField.Branch).action)
        assertEquals(OverviewRowAction.Configuration, row("finished", OverviewField.Configuration).action)
        assertEquals(OverviewRowAction.Project, row("finished", OverviewField.Project).action)
        assertEquals(OverviewRowAction.Copy, row("finished", OverviewField.Result).action)
    }

    @Test fun malformedAndMissingDatesHaveAnExplicitValue() {
        assertEquals(OverviewText.Unavailable, row("missing_dates", OverviewField.Time).value)
        val build = BuildOverviewFixtures.finished.copy(state = "running", startDate = "invalid")
        assertEquals(OverviewText.Unavailable, overviewRows(build).single { it.field == OverviewField.Time }.value)
    }

    @Test fun allResultIconsArePreserved() {
        assertEquals(OverviewIcon.Failure, row("failure", OverviewField.Result).icon)
        assertEquals(OverviewIcon.Error, row("status_error", OverviewField.Result).icon)
        assertEquals(OverviewIcon.Unknown, row("status_unknown", OverviewField.Result).icon)
    }
}
