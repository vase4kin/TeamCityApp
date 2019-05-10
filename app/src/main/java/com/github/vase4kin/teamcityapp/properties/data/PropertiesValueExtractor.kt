package com.github.vase4kin.teamcityapp.properties.data

import android.os.Bundle
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractorImpl

interface PropertiesValueExtractor : BaseValueExtractor

class PropertiesValueExtractorImpl(bundle: Bundle) : BaseValueExtractorImpl(bundle), PropertiesValueExtractor