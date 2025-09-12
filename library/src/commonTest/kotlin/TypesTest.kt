package com.bugsee.kmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TypesTest {

    @Test
    fun `test EventHandler type alias`() {
        // Test EventHandler<T> = ((T) -> Unit)
        val stringHandler: EventHandler<String> = { value ->
            assertNotNull(value)
        }
        
        val intHandler: EventHandler<Int> = { value ->
            assertTrue(value > 0)
        }
        
        val booleanHandler: EventHandler<Boolean> = { value ->
            assertTrue(value is Boolean)
        }
        
        // Test that handlers can be called
        stringHandler("test")
        intHandler(42)
        booleanHandler(true)
    }

    @Test
    fun `test TransformHandler type alias`() {
        // Test TransformHandler<T> = ((T) -> T)
        val stringTransform: TransformHandler<String> = { value ->
            value.uppercase()
        }
        
        val intTransform: TransformHandler<Int> = { value ->
            value * 2
        }
        
        val booleanTransform: TransformHandler<Boolean> = { value ->
            !value
        }
        
        // Test transformations
        assertEquals("HELLO", stringTransform("hello"))
        assertEquals(84, intTransform(42))
        assertEquals(false, booleanTransform(true))
    }

    @Test
    fun `test ProducerHandler type alias`() {
        // Test ProducerHandler<T> = () -> T
        val stringProducer: ProducerHandler<String> = { "test" }
        val intProducer: ProducerHandler<Int> = { 42 }
        val booleanProducer: ProducerHandler<Boolean> = { true }
        
        // Test producers
        assertEquals("test", stringProducer())
        assertEquals(42, intProducer())
        assertEquals(true, booleanProducer())
    }

    @Test
    fun `test ProducerArgHandler type alias`() {
        // Test ProducerArgHandler<T, R> = (T) -> R
        val stringToInt: ProducerArgHandler<String, Int> = { it.length }
        val intToString: ProducerArgHandler<Int, String> = { it.toString() }
        val booleanToInt: ProducerArgHandler<Boolean, Int> = { if (it) 1 else 0 }
        
        // Test argument handlers
        assertEquals(5, stringToInt("hello"))
        assertEquals("42", intToString(42))
        assertEquals(1, booleanToInt(true))
        assertEquals(0, booleanToInt(false))
    }

    @Test
    fun `test BugseeFeedbackEventListener type alias`() {
        // Test BugseeFeedbackEventListener = EventHandler<List<String>>
        val feedbackListener: BugseeFeedbackEventListener = { feedbackList ->
            assertNotNull(feedbackList)
            assertTrue(feedbackList is List<String>)
        }
        
        // Test feedback listener
        feedbackListener(listOf("feedback1", "feedback2"))
        feedbackListener(emptyList())
    }

    @Test
    fun `test BugseeReportFieldsFiller type alias`() {
        // Test BugseeReportFieldsFiller = EventHandler<BugseeReportFields>
        val reportFieldsFiller: BugseeReportFieldsFiller = { fields ->
            assertNotNull(fields)
            assertTrue(fields is BugseeReportFields)
        }
        
        // Test report fields filler
        val fields = BugseeReportFields("Test Summary", "Test Description", BugseeSeverity.High, listOf("test"))
        reportFieldsFiller(fields)
    }

    @Test
    fun `test BugseeReportFieldsFilter type alias`() {
        // Test BugseeReportFieldsFilter = TransformHandler<BugseeReportFields>
        val reportFieldsFilter: BugseeReportFieldsFilter = { fields ->
            assertNotNull(fields)
            fields // Return the same fields
        }
        
        // Test report fields filter
        val fields = BugseeReportFields("Test Summary", "Test Description", BugseeSeverity.High, listOf("test"))
        val filteredFields = reportFieldsFilter(fields)
        assertEquals(fields, filteredFields)
    }

    @Test
    fun `test BugseeNetworkFilter type alias`() {
        // Test BugseeNetworkFilter = TransformHandler<BugseeNetworkEvent?>
        val networkFilter: BugseeNetworkFilter = { event ->
            // Return the same event or null
            event
        }
        
        // Test network filter with null - just verify the type alias works
        assertNotNull(networkFilter)
    }

    @Test
    fun `test BugseeLogFilter type alias`() {
        // Test BugseeLogFilter = TransformHandler<BugseeLogEvent?>
        val logFilter: BugseeLogFilter = { event ->
            // Return the same event or null
            event
        }
        
        // Test log filter - just verify the type alias works
        assertNotNull(logFilter)
    }

    @Test
    fun `test BugseeLifecycleEventListener type alias`() {
        // Test BugseeLifecycleEventListener = EventHandler<BugseeLifecycleEvent>
        val lifecycleListener: BugseeLifecycleEventListener = { event ->
            assertNotNull(event)
            assertTrue(event is BugseeLifecycleEvent)
        }
        
        // Test lifecycle listener
        lifecycleListener(BugseeLifecycleEvent.Launched)
        lifecycleListener(BugseeLifecycleEvent.Stopped)
        lifecycleListener(BugseeLifecycleEvent.Paused)
        lifecycleListener(BugseeLifecycleEvent.Resumed)
    }

    @Test
    fun `test BugseeAttachmentsProvider type alias`() {
        // Test BugseeAttachmentsProvider = ProducerArgHandler<BugseeReport, List<BugseeAttachment>?>
        val attachmentsProvider: BugseeAttachmentsProvider = { report ->
            assertNotNull(report)
            assertTrue(report is BugseeReport)
            // Return null for this test
            null
        }
        
        // Test attachments provider - just verify the type alias works
        assertNotNull(attachmentsProvider)
    }

    @Test
    fun `test BugseeExtendedReportProvider type alias`() {
        // Test BugseeExtendedReportProvider = EventHandler<BugseeExtendedReport>
        val extendedReportProvider: BugseeExtendedReportProvider = { report ->
            assertNotNull(report)
            // Note: BugseeExtendedReport is expect class, so we can't instantiate it in common tests
        }
        
        // Test extended report provider
        // Note: We can't test with actual BugseeExtendedReport instance in common tests
    }

    @Test
    fun `test type alias composition`() {
        // Test that type aliases can be composed and used together
        
        // Create a chain of handlers
        val stringHandler: EventHandler<String> = { value ->
            assertNotNull(value)
        }
        
        val stringTransform: TransformHandler<String> = { value ->
            value.uppercase()
        }
        
        val stringProducer: ProducerHandler<String> = { "hello" }
        
        // Test composition
        val originalValue = stringProducer()
        val transformedValue = stringTransform(originalValue)
        stringHandler(transformedValue)
        
        assertEquals("HELLO", transformedValue)
    }

    @Test
    fun `test type alias with complex types`() {
        // Test type aliases with complex generic types
        
        val complexEventHandler: EventHandler<List<Map<String, Any>>> = { list ->
            assertNotNull(list)
            assertTrue(list is List<Map<String, Any>>)
        }
        
        val complexTransformHandler: TransformHandler<Map<String, List<Int>>> = { map ->
            assertNotNull(map)
            map
        }
        
        val complexProducerHandler: ProducerHandler<Map<String, Boolean>> = { 
            mapOf("test" to true, "debug" to false)
        }
        
        // Test complex handlers
        val complexData = mapOf("numbers" to listOf(1, 2, 3))
        val transformedData = complexTransformHandler(complexData)
        assertEquals(complexData, transformedData)
        
        val producedData = complexProducerHandler()
        assertEquals(2, producedData.size)
        assertEquals(true, producedData["test"])
        assertEquals(false, producedData["debug"])
        
        complexEventHandler(listOf(mapOf("key" to "value")))
    }

    @Test
    fun `test type alias nullability`() {
        // Test type aliases with nullable types
        
        val nullableEventHandler: EventHandler<String?> = { value ->
            // Should handle null values
            assertNotNull(value) // This will be null, but we can't assert null directly
        }
        
        val nullableTransformHandler: TransformHandler<String?> = { value ->
            value?.uppercase()
        }
        
        // Test nullable handlers - just verify the type aliases work
        assertNotNull(nullableEventHandler)
        assertNotNull(nullableTransformHandler)
        
        // Test with actual values
        nullableEventHandler("test")
        val stringResult = nullableTransformHandler("hello")
        assertEquals("HELLO", stringResult)
    }

    @Test
    fun `test type alias with unit return`() {
        // Test type aliases that return Unit
        
        val unitEventHandler: EventHandler<Unit> = { value ->
            assertNotNull(value)
        }
        
        val unitProducerHandler: ProducerHandler<Unit> = { 
            Unit
        }
        
        // Test unit handlers
        unitEventHandler(Unit)
        val unitValue = unitProducerHandler()
        assertEquals(Unit, unitValue)
    }
}
