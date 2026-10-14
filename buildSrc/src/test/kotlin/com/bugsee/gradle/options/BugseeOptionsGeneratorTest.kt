package com.bugsee.gradle.options

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class BugseeOptionsGeneratorTest {

    private fun option(
        key: String,
        type: String,
        default: Any?,
        enum: ManifestEnum? = null,
        module: String = "core",
        hidden: Boolean = false,
        deprecatedSince: String? = null,
    ) = ManifestOption(key, type, default, module, hidden, enum, deprecatedSince)

    private fun manifest(sdk: String, vararg options: ManifestOption) = OptionsManifest(sdk, "1.0.0", options.toList())

    private val androidSeverity = ManifestEnum("IssueSeverity", mapOf("VeryLow" to 1, "High" to 3))
    private val iosSeverity = ManifestEnum("BugseeSeverityLevel", mapOf("Low" to 1, "High" to 3))

    private val config = KmpOptionsConfig(
        names = mapOf(
            "k.duration" to "Duration",
            "k.max-data-size" to "MaxDataSize",
            "k.priority" to "Priority",
            "k.android-only" to "AndroidOnly",
            "k.ios-only" to "IosOnly",
            "k.ios-priority" to "IosPriority",
            "k.leak-mode" to "LeakMode",
            "k.scale" to "Scale",
            "k.text" to "Text",
        ),
        enums = mapOf(
            "IssueSeverity" to "BugseeSeverity",
            "BugseeSeverityLevel" to "BugseeSeverity",
            "MemoryLeakMode" to "BugseeMemoryLeakMode",
        ),
    )

    private fun generate(
        android: OptionsManifest = manifest(
            "android",
            option("k.duration", "int", 60),
            option("k.max-data-size", "int", 150),
            option("k.priority", "enum", "High", androidSeverity),
            option("k.android-only", "boolean", true, module = "core"),
            option("k.hidden", "string", null, hidden = true),
        ),
        ios: OptionsManifest = manifest(
            "ios",
            option("k.duration", "int", 60),
            option("k.max-data-size", "int", 50),
            option("k.priority", "enum", "Low", iosSeverity),
            option("k.ios-only", "string", null),
        ),
    ) = BugseeOptionsGenerator(config, android, ios).generate()

    @Test
    fun `same default is shared, different defaults branch on the platform`() {
        val spec = generate().getValue("BugseeOptionsSpec.kt")

        assertContains(spec, "put(BugseeOptions.Duration, 60)")
        assertContains(spec, "put(BugseeOptions.MaxDataSize, if (isAndroid) 150 else 50)")
    }

    @Test
    fun `iOS enum values reuse the Android names with the same Int value`() {
        val spec = generate().getValue("BugseeOptionsSpec.kt")

        // iOS "Low" (1) is Android "VeryLow" (1)
        assertContains(spec, "put(BugseeOptions.Priority, if (isAndroid) BugseeSeverity.High else BugseeSeverity.VeryLow)")
        assertContains(spec, "BugseeSeverity.VeryLow,")
        assertFalse("BugseeSeverity.Low," in spec)
    }

    @Test
    fun `single-platform options are known on their platform only`() {
        val files = generate()
        val spec = files.getValue("BugseeOptionsSpec.kt")
        val launchOptions = files.getValue("BugseeLaunchOptions.kt")

        assertContains(spec, "            put(BugseeOptions.AndroidOnly, true)")
        assertContains(launchOptions, "public var iosOnly: String?")
        assertContains(launchOptions, "store.stringOrNull(BugseeOptions.IosOnly, null)")
    }

    @Test
    fun `hidden options are not generated`() {
        assertFalse("k.hidden" in generate().getValue("BugseeOptions.kt"))
    }

    @Test
    fun `an option without a KMP name fails`() {
        val error = assertFailsWith<IllegalStateException> {
            generate(android = manifest("android", option("k.new-option", "boolean", false)))
        }
        assertContains(error.message!!, "k.new-option")
    }

    @Test
    fun `a native enum without a KMP enum fails`() {
        val error = assertFailsWith<IllegalStateException> {
            generate(
                android = manifest(
                    "android",
                    option("k.priority", "enum", "Fast", ManifestEnum("UnknownEnum", mapOf("Fast" to "Fast"))),
                ),
            )
        }
        assertContains(error.message!!, "UnknownEnum")
    }

    @Test
    fun `a type mismatch between platforms fails`() {
        assertFailsWith<IllegalStateException> {
            generate(ios = manifest("ios", option("k.duration", "float", 60.0)))
        }
    }

    @Test
    fun `iOS-only enum options reuse the Android names of the same KMP enum`() {
        val spec = generate(
            ios = manifest(
                "ios",
                option("k.priority", "enum", "Low", iosSeverity),
                option("k.ios-priority", "enum", "Low", iosSeverity),
            ),
        ).getValue("BugseeOptionsSpec.kt")

        assertContains(spec, "put(BugseeOptions.IosPriority, BugseeSeverity.VeryLow)")
        assertFalse("BugseeSeverity.Low," in spec)
    }

    @Test
    fun `string enums use the value as entry name, also for a constant-name default`() {
        val leakMode = ManifestEnum("MemoryLeakMode", mapOf("FAST" to "Fast", "DEEP_DEBUG_ONLY" to "DeepDebugOnly"))
        val spec = generate(android = manifest("android", option("k.leak-mode", "enum", "FAST", leakMode)))
            .getValue("BugseeOptionsSpec.kt")

        assertContains(spec, "put(BugseeOptions.LeakMode, BugseeMemoryLeakMode.Fast)")
        assertContains(spec, "BugseeMemoryLeakMode.DeepDebugOnly,")
        assertContains(spec, "\"deep_debug_only\" to BugseeMemoryLeakMode.DeepDebugOnly,")
        assertContains(spec, "android = mapOf<Enum<*>, Any>(BugseeMemoryLeakMode.Fast to \"FAST\", BugseeMemoryLeakMode.DeepDebugOnly to \"DEEP_DEBUG_ONLY\")")
        assertContains(spec, "ios = null")
    }

    @Test
    fun `floats render as Float literals, a fractional Int default fails`() {
        val spec = generate(ios = manifest("ios", option("k.scale", "float", 1))).getValue("BugseeOptionsSpec.kt")
        assertContains(spec, "put(BugseeOptions.Scale, 1.0f)")
        assertContains(spec, "BugseeOptions.Scale to BugseeOptionType.FloatType")

        assertFailsWith<IllegalStateException> {
            generate(android = manifest("android", option("k.duration", "int", 1.5)))
        }
    }

    @Test
    fun `string defaults are escaped in code and KDoc`() {
        val files = generate(android = manifest("android", option("k.text", "string", "a\"b\$c\nd*/e")))

        assertContains(files.getValue("BugseeOptionsSpec.kt"), """put(BugseeOptions.Text, "a\"b\${'$'}c\nd*/e")""")
        assertFalse("d*/e" in files.getValue("BugseeOptions.kt").substringBefore("public const val Text"))
    }

    @Test
    fun `an option is deprecated only when every platform deprecates it`() {
        val both = generate(
            android = manifest("android", option("k.duration", "int", 60, deprecatedSince = "7.1")),
            ios = manifest("ios", option("k.duration", "int", 60, deprecatedSince = "7.0")),
        ).getValue("BugseeLaunchOptions.kt")
        assertContains(both, "@Deprecated(\"Deprecated by the native SDK (Android since 7.1, iOS since 7.0).\")")

        val androidOnly = generate(
            android = manifest("android", option("k.duration", "int", 60, deprecatedSince = "7.1")),
        ).getValue("BugseeLaunchOptions.kt")
        assertFalse("@Deprecated" in androidOnly)
        assertContains(androidOnly, "Deprecated by the native SDK on Android since 7.1.")
    }

    @Test
    fun `enum types list the native values each platform accepts`() {
        val androidRate = ManifestEnum("FrameRate", mapOf("Low" to 1, "High" to 3, "Raw" to 4))
        val iosRate = ManifestEnum("BugseeFrameRate", mapOf("Low" to 1, "High" to 3))
        val rateConfig = config.copy(
            names = config.names + ("k.rate" to "Rate"),
            enums = config.enums + mapOf("FrameRate" to "BugseeFrameRate", "BugseeFrameRate" to "BugseeFrameRate"),
        )
        val spec = BugseeOptionsGenerator(
            rateConfig,
            manifest("android", option("k.rate", "enum", "High", androidRate)),
            manifest("ios", option("k.rate", "enum", "High", iosRate)),
        ).generate().getValue("BugseeOptionsSpec.kt")

        assertContains(spec, "4 to BugseeFrameRate.Raw,")
        assertContains(spec, "android = mapOf<Enum<*>, Any>(BugseeFrameRate.Low to \"Low\", BugseeFrameRate.High to \"High\", BugseeFrameRate.Raw to \"Raw\")")
        assertContains(spec, "ios = mapOf<Enum<*>, Any>(BugseeFrameRate.Low to 1, BugseeFrameRate.High to 3)")
    }

    @Test
    fun `duplicate KMP names fail`() {
        val duplicated = config.copy(names = config.names + ("k.ios-only" to "Duration"))
        assertFailsWith<IllegalStateException> {
            BugseeOptionsGenerator(
                duplicated,
                manifest("android", option("k.duration", "int", 60)),
                manifest("ios", option("k.ios-only", "string", null)),
            ).generate()
        }
    }
}
