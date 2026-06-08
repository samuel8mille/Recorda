package com.samuelribeiro.recorda.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

class RecordaIssueRegistry : IssueRegistry() {

    override val issues: List<Issue> = listOf(
        NoAndroidLogDetector.ISSUE,
        A11yHardcodedColorDetector.ISSUE
    )

    override val api: Int = CURRENT_API

    override val vendor = Vendor(
        vendorName = "Recorda",
        identifier = "com.samuelribeiro.recorda:lint"
    )
}
