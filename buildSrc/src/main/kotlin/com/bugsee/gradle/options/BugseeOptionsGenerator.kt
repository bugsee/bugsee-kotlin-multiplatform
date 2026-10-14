package com.bugsee.gradle.options

/**
 * Merges the Android and iOS option manifests and renders the common Kotlin sources:
 * `BugseeOptions` (keys), `BugseeLaunchOptions` (typed builder) and `BugseeOptionsSpec`
 * (per-platform defaults and known keys).
 *
 * Fails with [IllegalStateException] when the manifests and the hand-maintained
 * [KmpOptionsConfig] disagree, so a new SDK option cannot slip in unnamed. A KMP enum that
 * lacks a manifest value fails later, when the generated `BugseeOptionsSpec` is compiled.
 */
class BugseeOptionsGenerator(
    private val config: KmpOptionsConfig,
    private val android: OptionsManifest,
    private val ios: OptionsManifest,
) {
    private enum class KmpType(val kotlinName: String?) {
        BOOLEAN("Boolean"), INT("Int"), FLOAT("Float"), STRING("String"), ENUM(null)
    }

    private class Side(val option: ManifestOption, val default: String?)

    private class Merged(
        val key: String,
        val name: String,
        val type: KmpType,
        val kmpEnum: String?,
        val android: Side?,
        val ios: Side?,
        /** KMP entry -> Kotlin literal of the native value, per platform; null without the option. */
        val androidEnum: Map<String, String>? = null,
        val iosEnum: Map<String, String>? = null,
    ) {
        /** Deprecated only when every platform that has the option deprecates it. */
        val deprecated: Boolean get() = listOfNotNull(android, ios).all { it.option.deprecatedSince != null }
        val propertyName: String get() = name.replaceFirstChar { it.lowercaseChar() }
        val nullable: Boolean get() = listOfNotNull(android, ios).any { it.default == null }
        val kotlinType: String get() = (type.kotlinName ?: kmpEnum!!) + if (nullable) "?" else ""
        val fallback: String get() = (android ?: ios)!!.default ?: "null"
    }

    /** KMP enum class -> entry names, in manifest order. */
    private val enumEntries = LinkedHashMap<String, LinkedHashSet<String>>()

    /** Lower-cased names and native Int values that resolve to an entry of one KMP enum. */
    private class EnumTable(val kmpEnum: String) {
        val names = LinkedHashMap<String, String>()
        val values = LinkedHashMap<Int, String>()

        fun addName(name: String, entry: String) = add(names, name.lowercase(), entry)
        fun addValue(value: Int, entry: String) = add(values, value, entry)

        private fun <K> add(map: MutableMap<K, String>, key: K, entry: String) {
            val existing = map.putIfAbsent(key, entry)
            check(existing == null || existing == entry) {
                "$kmpEnum: '$key' resolves to both $existing and $entry"
            }
        }
    }

    private val enumTables = LinkedHashMap<String, EnumTable>()

    /** Warnings that do not stop generation (e.g. names for keys no manifest has). */
    val warnings = mutableListOf<String>()

    /** KMP enum class -> the Android native enum it maps to; iOS values are named after it. */
    private val androidEnums: Map<String, ManifestEnum> = android.options
        .filterNot { it.hidden }
        .mapNotNull { option -> option.enum?.let { config.enums[it.name]?.to(it) } }
        .toMap()

    private val options: List<Merged> = merge()

    /** File name -> content, all in package `com.bugsee.kmp`. */
    fun generate(): Map<String, String> = mapOf(
        "BugseeOptions.kt" to renderKeys(),
        "BugseeLaunchOptions.kt" to renderLaunchOptions(),
        "BugseeOptionsSpec.kt" to renderSpec(),
    )

    private fun merge(): List<Merged> {
        val androidByKey = android.options.filterNot { it.hidden }.associateBy { it.key }
        val iosByKey = ios.options.filterNot { it.hidden }.associateBy { it.key }
        val keys = (androidByKey.keys + iosByKey.keys).sorted()

        val unnamed = keys.filter { it !in config.names }
        check(unnamed.isEmpty()) {
            "No KMP name for option(s) ${unnamed.joinToString()}. Add them to the \"names\" map in " +
                "library/options/kmp-options.json (use the native Android Options.* name when there is one)."
        }
        (config.names.keys - keys.toSet()).forEach { warnings += "kmp-options.json names unknown key $it" }
        val duplicates = config.names.filterKeys { it in keys }.values.groupingBy { it }.eachCount().filterValues { it > 1 }
        check(duplicates.isEmpty()) { "Duplicate KMP option names: ${duplicates.keys.joinToString()}" }

        return keys.map { key -> mergeOption(key, androidByKey[key], iosByKey[key]) }
    }

    private fun mergeOption(key: String, a: ManifestOption?, i: ManifestOption?): Merged {
        val type = listOfNotNull(a, i).map { kmpType(it) }.distinct().singleOrNull()
            ?: error("Option $key has a different type on Android (${a?.type}) and iOS (${i?.type})")

        val kmpEnum = if (type == KmpType.ENUM) {
            listOfNotNull(a, i).map { option ->
                config.enums[option.enum!!.name]
                    ?: error("No KMP enum for native enum ${option.enum.name} (option $key). Add it to the \"enums\" map in library/options/kmp-options.json.")
            }.distinct().singleOrNull() ?: error("Option $key maps to different KMP enums on Android and iOS")
        } else {
            null
        }

        // Android names win: an iOS value with the same native Int value reuses the Android name,
        // even when the iOS option has no Android counterpart.
        val androidEntries = a?.enum?.let { entryNames(it, null) } ?: emptyMap()
        val iosEntries = i?.enum?.let { entryNames(it, kmpEnum?.let(androidEnums::get)) } ?: emptyMap()
        if (kmpEnum != null) {
            enumEntries.getOrPut(kmpEnum) { LinkedHashSet() }.apply {
                addAll(androidEntries.values)
                addAll(iosEntries.values)
            }
            val table = enumTables.getOrPut(kmpEnum) { EnumTable(kmpEnum) }
            listOfNotNull(a?.enum?.to(androidEntries), i?.enum?.to(iosEntries)).forEach { (nativeEnum, entries) ->
                nativeEnum.values.forEach { (constant, value) ->
                    val entry = entries.getValue(constant)
                    table.addName(entry, entry)
                    table.addName(constant, entry)
                    if (value is String) table.addName(value, entry) else table.addValue((value as Number).toInt(), entry)
                }
            }
        }

        return Merged(
            key = key,
            name = config.names.getValue(key),
            type = type,
            kmpEnum = kmpEnum,
            android = a?.let { Side(it, renderDefault(it, type, kmpEnum, androidEntries)) },
            ios = i?.let { Side(it, renderDefault(it, type, kmpEnum, iosEntries)) },
            // Android resolves enum options by constant name; iOS takes the native value.
            androidEnum = a?.enum?.let { androidEntries.entries.associate { (constant, entry) -> entry to kotlinString(constant) } },
            iosEnum = i?.enum?.let { nativeEnum ->
                iosEntries.entries.associate { (constant, entry) -> entry to nativeLiteral(nativeEnum.values.getValue(constant)) }
            },
        )
    }

    private fun nativeLiteral(value: Any): String = when (value) {
        is String -> kotlinString(value)
        else -> (value as Number).toInt().toString()
    }

    private fun kmpType(option: ManifestOption): KmpType = when (option.type) {
        "boolean" -> KmpType.BOOLEAN
        "int" -> KmpType.INT
        "float" -> KmpType.FLOAT
        "string" -> KmpType.STRING
        "enum" -> KmpType.ENUM
        else -> error("Option ${option.key} has unsupported type '${option.type}'")
    }

    /**
     * Native constant name -> KMP entry name. String-valued enums (e.g. Android MemoryLeakMode,
     * `FAST` -> "Fast") use the value; Int-valued ones use the constant name, or the [reference]
     * enum's name for the same Int value.
     */
    private fun entryNames(nativeEnum: ManifestEnum, reference: ManifestEnum?): Map<String, String> =
        nativeEnum.values.mapValues { (constant, value) ->
            when (value) {
                is String -> value
                else -> reference?.values?.entries
                    ?.firstOrNull { (_, refValue) -> (refValue as? Number)?.toInt() == (value as Number).toInt() }
                    ?.key
                    ?: constant
            }
        }

    private fun renderDefault(option: ManifestOption, type: KmpType, kmpEnum: String?, entries: Map<String, String>): String? {
        val value = option.default ?: return null.also {
            check(type == KmpType.STRING) { "Option ${option.key} (${option.type}) has no default; only string options may" }
        }
        return when (type) {
            KmpType.BOOLEAN -> (value as Boolean).toString()
            KmpType.INT -> {
                val number = value as Number
                check(number.toDouble() == number.toInt().toDouble()) { "Option ${option.key}: default $value is not an Int" }
                number.toInt().toString()
            }
            KmpType.FLOAT -> "${(value as Number).toDouble()}f"
            KmpType.STRING -> kotlinString(value as String)
            KmpType.ENUM -> {
                val entry = entries[value as String]
                    ?: error("Option ${option.key}: default '$value' is not a value of ${option.enum?.name}")
                "$kmpEnum.$entry"
            }
        }
    }

    // --- rendering -------------------------------------------------------------------------

    private fun header(): String =
        "// GENERATED by :library:generateBugseeOptions from library/options/" +
            "android-${android.sdkVersion}.json, ios-${ios.sdkVersion}.json and kmp-options.json.\n" +
            "// Do not edit: change the inputs and rebuild.\n"

    private fun kdoc(option: Merged, indent: String): String {
        val platforms = when {
            option.android != null && option.ios != null -> "Android and iOS."
            option.android != null -> "Android only" + moduleNote(option.android.option.module, "bugsee-android") + "."
            else -> "iOS only" + moduleNote(option.ios!!.option.module, "Bugsee") + "."
        }
        val a = option.android?.default
        val i = option.ios?.default
        val defaults = when {
            option.android != null && option.ios != null && a != i -> "Default: ${code(a)} on Android, ${code(i)} on iOS."
            else -> "Default: ${code(a ?: i)}."
        }
        val deprecations = listOfNotNull(
            option.android?.option?.deprecatedSince?.let { "Android since $it" },
            option.ios?.option?.deprecatedSince?.let { "iOS since $it" },
        )
        return buildString {
            append("$indent/**\n")
            append("$indent * ${kdocSafe("Option `${option.key}`. $platforms")}\n")
            append("$indent *\n")
            append("$indent * ${kdocSafe(defaults)}\n")
            if (deprecations.isNotEmpty() && !option.deprecated) {
                append("$indent *\n")
                append("$indent * Deprecated by the native SDK on ${deprecations.joinToString()}.\n")
            }
            append("$indent */\n")
            if (option.deprecated) {
                val text = "Deprecated by the native SDK (${deprecations.joinToString()})."
                append("$indent@Deprecated(${kotlinString(text)})\n")
            }
        }
    }

    // A "*/" inside a default or key would end the KDoc comment.
    private fun kdocSafe(text: String): String = text.replace("*/", "*&#47;")

    private fun moduleNote(module: String, core: String): String =
        if (module == core) "" else ", requires the `$module` module"

    private fun code(literal: String?): String = "`${literal ?: "null"}`"

    private fun renderKeys(): String = buildString {
        append(header())
        append("package com.bugsee.kmp\n\n")
        append("/**\n")
        append(" * Launch option keys of the Bugsee Android SDK ${android.sdkVersion} and iOS SDK ${ios.sdkVersion}.\n")
        append(" *\n")
        append(" * Pass them to the `Map` overload of [Bugsee.launch] or to [BugseeLaunchOptions.setCustomOption];\n")
        append(" * [BugseeLaunchOptions] exposes every key as a typed property.\n")
        append(" */\n")
        append("public object BugseeOptions {\n")
        options.forEachIndexed { index, option ->
            if (index > 0) append("\n")
            append(kdoc(option, "    "))
            append("    public const val ${option.name}: String = ${kotlinString(option.key)}\n")
        }
        append("}\n")
    }

    private fun renderLaunchOptions(): String = buildString {
        append(header())
        append("@file:Suppress(\"DEPRECATION\")\n\n")
        append("package com.bugsee.kmp\n\n")
        append("import com.bugsee.kmp.internal.Platform\n")
        append("import com.bugsee.kmp.internal.PlatformInfo\n\n")
        append("/**\n")
        append(" * Typed launch options for [Bugsee.launch].\n")
        append(" *\n")
        append(" * Every property starts at the native SDK default of the current platform, and every option\n")
        append(" * the current platform knows is passed to the native SDK on launch.\n")
        append(" */\n")
        append("public class BugseeLaunchOptions() {\n")
        append("    private val store = BugseeLaunchOptionsStore(PlatformInfo.getPlatformType() == Platform.ANDROID)\n\n")
        append("    /**\n")
        append("     * Creates options from a raw map. [BugseeOptions] keys set the matching property; other keys\n")
        append("     * are passed to the native SDK unchanged.\n")
        append("     */\n")
        append("    public constructor(options: Map<String, Any>) : this() {\n")
        append("        store.putAll(options)\n")
        append("    }\n\n")
        append("    /**\n")
        append("     * Sets an option by its raw key. A [BugseeOptions] key sets the matching property; any other\n")
        append("     * key is passed to the native SDK unchanged.\n")
        append("     */\n")
        append("    public fun setCustomOption(key: String, value: Any) {\n")
        append("        store.put(key, value)\n")
        append("    }\n\n")
        append("    internal fun toMap(): Map<String, Any> = store.toMap()\n")
        options.forEach { option ->
            append("\n")
            append(kdoc(option, "    "))
            append("    public var ${option.propertyName}: ${option.kotlinType}\n")
            append("        get() = ${getter(option)}\n")
            append("        set(value) = store.put(BugseeOptions.${option.name}, value)\n")
        }
        append("}\n")
    }

    private fun getter(option: Merged): String {
        val key = "BugseeOptions.${option.name}"
        return when (option.type) {
            KmpType.BOOLEAN -> "store.boolean($key, ${option.fallback})"
            KmpType.INT -> "store.int($key, ${option.fallback})"
            KmpType.FLOAT -> "store.float($key, ${option.fallback})"
            KmpType.STRING -> if (option.nullable) "store.stringOrNull($key, ${option.fallback})" else "store.string($key, ${option.fallback})"
            KmpType.ENUM -> "store.enum($key, ${option.fallback})"
        }
    }

    private fun renderSpec(): String = buildString {
        val both = options.filter { it.android != null && it.ios != null }
        val androidOnly = options.filter { it.android != null && it.ios == null }
        val iosOnly = options.filter { it.android == null && it.ios != null }

        fun keySet(list: List<Merged>) = list.joinToString(",\n", postfix = ",\n") { "        BugseeOptions.${it.name}" }
        fun put(option: Merged, value: String, indent: String) = "${indent}put(BugseeOptions.${option.name}, $value)\n"

        append(header())
        append("@file:Suppress(\"DEPRECATION\")\n\n")
        append("package com.bugsee.kmp\n\n")
        append("internal object BugseeOptionsSpec {\n")
        append("    /** Every option key of either platform. */\n")
        append("    val allKeys: Set<String> = androidKeys() + iosKeys()\n\n")
        append("    fun knownKeys(isAndroid: Boolean): Set<String> = if (isAndroid) androidKeys() else iosKeys()\n\n")
        append("    private fun androidKeys(): Set<String> = setOf(\n${keySet(both + androidOnly)}    )\n\n")
        append("    private fun iosKeys(): Set<String> = setOf(\n${keySet(both + iosOnly)}    )\n\n")
        append("    /** Native SDK defaults of the current platform; options without a default are absent. */\n")
        append("    fun defaults(isAndroid: Boolean): Map<String, Any> = buildMap {\n")
        append("        // Android and iOS\n")
        both.forEach { option ->
            val a = option.android!!.default
            val i = option.ios!!.default
            when {
                a != null && a == i -> append(put(option, a, "        "))
                a != null && i != null -> append(put(option, "if (isAndroid) $a else $i", "        "))
                a != null -> append("        if (isAndroid) ${put(option, a, "").trimEnd()}\n")
                i != null -> append("        if (!isAndroid) ${put(option, i, "").trimEnd()}\n")
            }
        }
        append("\n        if (isAndroid) {\n")
        androidOnly.forEach { option -> option.android!!.default?.let { append(put(option, it, "            ")) } }
        append("        } else {\n")
        iosOnly.forEach { option -> option.ios!!.default?.let { append(put(option, it, "            ")) } }
        append("        }\n")
        append("    }\n\n")
        enumTables.values.forEach { table ->
            val prefix = table.kmpEnum.replaceFirstChar { it.lowercaseChar() }
            append("    private val ${prefix}Names: Map<String, Enum<*>> = mapOf(\n")
            table.names.forEach { (name, entry) -> append("        ${kotlinString(name)} to ${table.kmpEnum}.$entry,\n") }
            append("    )\n\n")
            append("    private val ${prefix}Values: Map<Int, Enum<*>> = mapOf(\n")
            table.values.forEach { (value, entry) -> append("        $value to ${table.kmpEnum}.$entry,\n") }
            append("    )\n\n")
        }
        append("    /** Value type of every option, used to reject values the native SDKs cannot read. */\n")
        append("    val types: Map<String, BugseeOptionType> = mapOf(\n")
        options.forEach { option ->
            val type = when (option.type) {
                KmpType.BOOLEAN -> "BugseeOptionType.BooleanType"
                KmpType.INT -> "BugseeOptionType.IntType"
                KmpType.FLOAT -> "BugseeOptionType.FloatType"
                KmpType.STRING -> "BugseeOptionType.StringType"
                KmpType.ENUM -> enumType(option)
            }
            append("        BugseeOptions.${option.name} to $type,\n")
        }
        append("    )\n\n")
        append("    /**\n")
        append("     * Every enum value the native manifests list. Compiling this fails when a KMP enum misses\n")
        append("     * one: add the value to the enum.\n")
        append("     */\n")
        append("    val manifestEnumEntries: List<Enum<*>> = listOf(\n")
        enumEntries.forEach { (kmpEnum, entries) ->
            entries.forEach { append("        $kmpEnum.$it,\n") }
        }
        append("    )\n")
        append("}\n")
    }

    private fun enumType(option: Merged): String {
        val prefix = option.kmpEnum!!.replaceFirstChar { it.lowercaseChar() }
        fun nativeMap(map: Map<String, String>?): String = map?.entries?.joinToString(
            separator = ", ",
            prefix = "mapOf<Enum<*>, Any>(",
            postfix = ")",
        ) { (entry, native) -> "${option.kmpEnum}.$entry to $native" } ?: "null"

        return "BugseeOptionType.EnumType(\n" +
            "            names = ${prefix}Names,\n" +
            "            values = ${prefix}Values,\n" +
            "            android = ${nativeMap(option.androidEnum)},\n" +
            "            ios = ${nativeMap(option.iosEnum)},\n" +
            "        )"
    }

    private fun kotlinString(value: String): String = "\"" + value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("$", "\\$")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t") + "\""
}
