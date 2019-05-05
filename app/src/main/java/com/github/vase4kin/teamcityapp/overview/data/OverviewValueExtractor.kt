package com.github.vase4kin.teamcityapp.overview.data

import android.os.Bundle
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractorImpl

interface OverviewValueExtractor : BaseValueExtractor

class OverviewValueExtractorImpl(bundle: Bundle) : BaseValueExtractorImpl(bundle), OverviewValueExtractor