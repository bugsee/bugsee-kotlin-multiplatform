package com.bugsee.kmp

// TODO: Ensure all the values here are actually
//  mapping correctly to the values in the underlying SDKs!

public enum class BugseeLifecycleEvent(private val eventType: Int) {
    /**
     * Event is dispatched when Bugsee was successfully launched
     */
    Launched(0),
    /**
     * Event is dispatched when Bugsee is started after being stopped
     */
    Started(1),
    /**
     * Event is dispatched when Bugsee is stopped
     */
    Stopped(2),
    /**
     * Event is dispatched when Bugsee recording is resumed after being paused
     */
    Resumed(3),
    /**
     * Event is dispatched when Bugsee recording is paused
     */
    Paused(4),
    /**
     * Event is dispatched when Bugsee is launched and pending crash report is
     * discovered. That usually means that app was relaunched after crash.
     */
    RelaunchedAfterCrash(5),
    /**
     * Event is dispatched before the reporting UI is shown
     */
    BeforeReportShown(6),
    /**
     * Event is dispatched when reporting UI is shown
     */
    AfterReportShown(7),
    /**
     * Event is dispatched when report is about to be uploaded to the server
     */
    BeforeReportUploaded(8),
    /**
     * Event is dispatched when report was successfully uploaded to the server
     */
    AfterReportUploaded(9),
    /**
     * Event is dispatched before the Feedback controller is shown
     */
    BeforeFeedbackShown(10),
    /**
     * Event is dispatched after the Feedback controller is shown
     */
    AfterFeedbackShown(11),
    /**
     * Event is dispatched before the bug/crash/error report assembly starts
     */
    BeforeReportAssembled(12),
    /**
     * Event is dispatched after the bug/crash/error report assembly completes
     */
    AfterReportAssembled(13),
    /**
     * Event is dispatched after bug/error/crash report upload failed
     * and will be retried in the future
     */
    ReportUploadFailedWithFutureRetry(14),
    /**
     * Event is dispatched after multiple bug/error/crash report upload
     * attempts ended with failure. It indicates no more attempts will
     * be taken and the report will not be uploaded.
     */
    ReportUploadFailed(15);

    public fun toIntValue(): Int {
        return eventType
    }

    public fun toLongValue(): ULong {
        return eventType.toULong()
    }
}