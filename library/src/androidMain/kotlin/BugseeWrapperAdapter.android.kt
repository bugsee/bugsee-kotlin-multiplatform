package com.bugsee.kmp

import com.bugsee.library.contracts.common.DataRequestResultCallback
import com.bugsee.library.contracts.internal.BugseeWrapper

/**
 * Describes this library to Android SDK 7 through `Bugsee.setWrapper`, the SDK's channel for
 * cross-platform wrappers, as `BugseeWrapperAdapter` does on iOS. The SDK attaches the type,
 * version, build and context to every session as `sdk.wrapper`, whichever launch or relaunch
 * started it; a registered wrapper takes precedence over the hidden `$$WRAPPER` launch option.
 *
 * Only the metadata and the required data-request callback are implemented; the report handler,
 * lifecycle, channel and secure-rectangle methods keep the interface's no-op defaults.
 */
internal class BugseeWrapperAdapter : BugseeWrapper {
    override fun getWrapperType(): String = BugseeWrapperInfo.TYPE

    override fun getWrapperVersion(): String = BugseeWrapperInfo.version

    override fun getWrapperBuild(): String = BugseeWrapperInfo.build

    override fun getContext(): Map<String, String> = mapOf("runtime" to BugseeWrapperInfo.runtime)

    // The SDK asks a wrapper for data of its own runtime by type; this library has none to add.
    // The callback is answered at once, so the SDK does not wait on it.
    override fun requestData(dataType: String, callback: DataRequestResultCallback) {
        callback.onResult(null)
    }
}
