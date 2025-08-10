package com.bugsee.kmp


public typealias EventHandler<T> = ((T) -> Unit)
public typealias TransformHandler<T> = ((T) -> T)
public typealias ProducerHandler<T> = () -> T
public typealias ProducerArgHandler<T, R> = (T) -> R

public typealias BugseeFeedbackEventListener = EventHandler<List<String>>
public typealias BugseeReportFieldsFiller = EventHandler<BugseeReportFields>
public typealias BugseeReportFieldsFilter = TransformHandler<BugseeReportFields>

public typealias BugseeNetworkFilter = TransformHandler<BugseeNetworkEvent?>
public typealias BugseeLogFilter = TransformHandler<BugseeLogEvent?>
public typealias BugseeLifecycleEventListener = EventHandler<BugseeLifecycleEvent>
public typealias BugseeAttachmentsProvider = ProducerArgHandler<BugseeReport, List<BugseeAttachment>?>
public typealias BugseeExtendedReportProvider = EventHandler<BugseeExtendedReport>