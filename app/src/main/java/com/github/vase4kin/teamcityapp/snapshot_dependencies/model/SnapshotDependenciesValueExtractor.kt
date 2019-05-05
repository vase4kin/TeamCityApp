package com.github.vase4kin.teamcityapp.snapshot_dependencies.model

import android.os.Bundle
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractorImpl

interface SnapshotDependenciesValueExtractor : BaseValueExtractor

class SnapshotDependenciesValueExtractorImpl(bundle: Bundle) : BaseValueExtractorImpl(bundle), SnapshotDependenciesValueExtractor