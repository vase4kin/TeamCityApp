package com.github.vase4kin.teamcityapp.api

import com.github.vase4kin.teamcityapp.account.create.helper.UrlFormatter
import com.github.vase4kin.teamcityapp.api.cache.CacheProviders
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RepositoryImplTest {

    @Mock
    private lateinit var teamCityService: TeamCityService
    @Mock
    private lateinit var cacheProviders: CacheProviders
    @Mock
    private lateinit var urlFormatter: UrlFormatter
    private lateinit var repository: RepositoryImpl

    @Before
    fun setUp() {
        repository = RepositoryImpl(teamCityService, cacheProviders, urlFormatter)
    }

    @Test
    fun listBuildsWithBlankBuildTypeIdReturnsAnError() {
        for (id in listOf("", " ")) {
            repository.listBuilds(id, "count:10", false)
                .test()
                .assertError { error ->
                    error is IllegalArgumentException && error.message == "Build type ID is missing"
                }
        }

        verifyNoInteractions(teamCityService, cacheProviders)
    }
}
