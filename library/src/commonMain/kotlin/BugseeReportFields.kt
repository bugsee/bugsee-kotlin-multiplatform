package com.bugsee.kmp

public class BugseeReportFields(
    public var summary: String,
    public var description: String,
    public var severity: BugseeSeverity,
    public var labels: List<String>
)