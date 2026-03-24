package com.bugsee.kmp

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding

public actual class BugseeNetworkEvent (internal val impl: cocoapods.Bugsee.BugseeNetworkEvent) {

    public actual val method: String
        get() = impl.method

    public actual val stage: BugseeNetworkEventStage
        get() = BugseeIOSUtils.convertNetworkEventStage(impl.bugseeNetworkEventType)

    public actual val responseCode: Int
        get() = impl.responseCode.toInt()

    public actual val noBodyReason: String?
        get() = impl.noBodyReason

    // Android-only field — no iOS SDK equivalent
    public actual val errorDescription: String?
        get() = null

    // Android-only field — no iOS SDK equivalent
    public actual val errorShort: String?
        get() = null

    public actual var url: String?
        get() = impl.url ?: ""
        set(value) {
            impl.url = value
        }

    @OptIn(BetaInteropApi::class)
    public actual var body: String?
        get() {
            if (impl.body == null) return null

            return NSString.create(impl.body!!, encoding = NSUTF8StringEncoding)?.toString()
        }
        set(value) {
            impl.body = value?.let { (it as NSString).dataUsingEncoding(NSUTF8StringEncoding) }
        }

    public actual var headers: Map<String, Any>?
        get() {
            @Suppress("UNCHECKED_CAST")
            return impl.headers as? Map<String, Any>
        }
        set(value) {
            @Suppress("UNCHECKED_CAST")
            impl.headers = value as? Map<Any?, *>
        }
}