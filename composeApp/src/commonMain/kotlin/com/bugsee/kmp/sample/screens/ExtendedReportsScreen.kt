package com.bugsee.kmp.sample.screens

import androidx.compose.runtime.Composable
import com.bugsee.kmp.Bugsee

@Composable
fun ExtendedReportsScreen(onBackClick: () -> Unit) {
    TestScreenScaffold(title = "Extended Reports", onBackClick = onBackClick) {
        TestButton("Send ExtendedReport with PNG") {
            Bugsee.createReport { report ->
                report.screenshot = createTestPng()
                report.summary = "Test PNG from KMP"
                report.addLabel("png-test")
                report.setAttribute("kmp", "attr-kmp")
                Bugsee.upload(report)
            }
        }
    }
}

/**
 * Creates a 100x100 gradient PNG as ByteArray for testing screenshot setting from common code.
 */
internal fun createTestPng(): ByteArray {
    val width = 100
    val height = 100

    val signature = byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    )

    fun intToBytes(value: Int): ByteArray = byteArrayOf(
        (value shr 24 and 0xFF).toByte(),
        (value shr 16 and 0xFF).toByte(),
        (value shr 8 and 0xFF).toByte(),
        (value and 0xFF).toByte()
    )

    fun chunk(type: ByteArray, data: ByteArray): ByteArray {
        val crcInput = type + data
        var crc = 0xFFFFFFFFL
        for (b in crcInput) {
            crc = crc xor (b.toLong() and 0xFF)
            repeat(8) {
                crc = if (crc and 1L != 0L) (crc shr 1) xor 0xEDB88320L else crc shr 1
            }
        }
        crc = crc xor 0xFFFFFFFFL
        return intToBytes(data.size) + type + data + intToBytes(crc.toInt())
    }

    val ihdrData = intToBytes(width) + intToBytes(height) + byteArrayOf(8, 6, 0, 0, 0)
    val ihdr = chunk("IHDR".encodeToByteArray(), ihdrData)

    val rawData = ByteArray(height * (1 + width * 4))
    var offset = 0
    for (y in 0 until height) {
        rawData[offset++] = 0
        for (x in 0 until width) {
            rawData[offset++] = (255 * x / width).toByte()
            rawData[offset++] = (255 * y / height).toByte()
            rawData[offset++] = (255 - 255 * x / width).toByte()
            rawData[offset++] = 0xFF.toByte()
        }
    }

    val maxBlock = 65535
    val blocks = mutableListOf<ByteArray>()
    var pos = 0
    while (pos < rawData.size) {
        val remaining = rawData.size - pos
        val blockSize = minOf(remaining, maxBlock)
        val isFinal = pos + blockSize >= rawData.size
        val header = byteArrayOf(
            if (isFinal) 0x01 else 0x00,
            (blockSize and 0xFF).toByte(), (blockSize shr 8 and 0xFF).toByte(),
            (blockSize.inv() and 0xFF).toByte(), (blockSize.inv() shr 8 and 0xFF).toByte()
        )
        blocks.add(header + rawData.copyOfRange(pos, pos + blockSize))
        pos += blockSize
    }

    val zlibHeader = byteArrayOf(0x78.toByte(), 0x01.toByte())
    val deflateBody = blocks.fold(byteArrayOf()) { acc, block -> acc + block }
    val deflateData = zlibHeader + deflateBody + adler32(rawData)

    val idat = chunk("IDAT".encodeToByteArray(), deflateData)
    val iend = chunk("IEND".encodeToByteArray(), byteArrayOf())

    return signature + ihdr + idat + iend
}

private fun adler32(data: ByteArray): ByteArray {
    var a = 1L
    var b = 0L
    for (byte in data) {
        a = (a + (byte.toLong() and 0xFF)) % 65521
        b = (b + a) % 65521
    }
    val value = (b shl 16) or a
    return byteArrayOf(
        (value shr 24 and 0xFF).toByte(),
        (value shr 16 and 0xFF).toByte(),
        (value shr 8 and 0xFF).toByte(),
        (value and 0xFF).toByte()
    )
}
