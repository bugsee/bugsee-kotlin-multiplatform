package com.bugsee.kmp

public class BugseeReport internal constructor(
    public val type: BugseeReportType,
    public val severity: BugseeSeverity,
    public val labels: List<String>?
)