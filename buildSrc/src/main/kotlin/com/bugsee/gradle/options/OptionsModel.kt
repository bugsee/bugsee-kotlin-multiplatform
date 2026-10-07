package com.bugsee.gradle.options

import groovy.json.JsonSlurper
import java.io.File

/**
 * One native SDK option manifest, as published at
 * `https://download.bugsee.com/sdk/options/<platform>/<version>.json`.
 */
data class OptionsManifest(
    val sdk: String,
    val sdkVersion: String,
    val options: List<ManifestOption>,
)

data class ManifestOption(
    val key: String,
    val type: String,
    val default: Any?,
    val module: String,
    val hidden: Boolean,
    val enum: ManifestEnum?,
    val deprecatedSince: String?,
)

/** `values` maps a native enum constant name to its native value (an Int, or a String for string enums). */
data class ManifestEnum(
    val name: String,
    val values: Map<String, Any>,
)

/** Hand-maintained generator input (`library/options/kmp-options.json`). */
data class KmpOptionsConfig(
    /** Option key -> generated name (PascalCase). */
    val names: Map<String, String>,
    /** Native manifest enum name -> KMP enum class name. */
    val enums: Map<String, String>,
)

internal object OptionsJson {

    private const val SUPPORTED_MANIFEST_VERSION = 1

    @Suppress("UNCHECKED_CAST")
    fun readManifest(file: File): OptionsManifest {
        val root = parse(file) as Map<String, Any?>
        val manifestVersion = (root["manifestVersion"] as? Number)?.toInt()
        require(manifestVersion == SUPPORTED_MANIFEST_VERSION) {
            "$file: unsupported manifestVersion $manifestVersion (expected $SUPPORTED_MANIFEST_VERSION)"
        }

        val options = (root["options"] as List<Map<String, Any?>>).map { option ->
            val enumJson = option["enum"] as Map<String, Any?>?
            ManifestOption(
                key = option["key"] as String,
                type = option["type"] as String,
                default = option["default"],
                module = option["module"] as String,
                hidden = option["hidden"] as? Boolean ?: false,
                enum = enumJson?.let {
                    ManifestEnum(it["name"] as String, it["values"] as Map<String, Any>)
                },
                deprecatedSince = (option["deprecated"] as Map<String, Any?>?)?.get("since") as String?,
            )
        }

        return OptionsManifest(root["sdk"] as String, root["sdkVersion"] as String, options)
    }

    @Suppress("UNCHECKED_CAST")
    fun readConfig(file: File): KmpOptionsConfig {
        val root = parse(file) as Map<String, Any?>
        return KmpOptionsConfig(
            names = root["names"] as Map<String, String>,
            enums = root["enums"] as Map<String, String>,
        )
    }

    private fun parse(file: File): Any = JsonSlurper().parse(file)
}
