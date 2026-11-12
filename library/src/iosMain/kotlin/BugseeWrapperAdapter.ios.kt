package com.bugsee.kmp

import platform.darwin.NSObject
import swiftPMImport.com.bugsee.library.BGSDataRequestResultCallbackProtocol
import swiftPMImport.com.bugsee.library.BugseeWrapperProtocol

/**
 * Describes this library to iOS SDK 7 through `+[Bugsee setWrapper:]` (the `Bugsee.SPI`
 * submodule), the SDK's channel for cross-platform wrappers. The SDK attaches the type, version,
 * build and context to the session as `sdk.wrapper`, the same data Android gets from the
 * `$$WRAPPER` launch option. It reads the legacy `wrapper_info` launch option only when no adapter
 * is registered.
 *
 * Only the metadata and the required data-request callback are implemented; the optional parts of
 * the protocol (report handler, lifecycle events, secure rectangles, data channel) are not used.
 */
internal class BugseeWrapperAdapter : NSObject(), BugseeWrapperProtocol {
    override fun wrapperType(): String = BugseeWrapperInfo.TYPE

    override fun wrapperVersion(): String = BugseeWrapperInfo.version

    override fun wrapperBuild(): String = BugseeWrapperInfo.build

    override fun context(): Map<Any?, *> = mapOf("runtime" to BugseeWrapperInfo.runtime)

    // The SDK asks a wrapper for data of its own runtime by type; this library has none to add.
    // The callback is answered at once, so the SDK does not wait on it.
    override fun requestDataWithType(dataType: String, callback: BGSDataRequestResultCallbackProtocol) {
        callback.onResult(null)
    }
}
