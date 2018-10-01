package hackernewslater.williamha.com.hackernewslater

import hackernewslater.williamha.com.hackernewslater.ui.utilities.HNLDateTime
import org.junit.Test

import org.junit.Assert.*

class HNLDateTimeTest {
    @Test
    fun check_gmt_to_local_time() {
        val dateTime = HNLDateTime.newInstance()
        val localTime = dateTime.convertGMTToLocalHour(0)
        assertEquals(localTime, 17) // In military time
    }

    @Test
    fun check_24_to_12_conversion() {
        val dateTime = HNLDateTime.newInstance()
        val time = dateTime.convert24HourMinuteTo12(17,0)
        assertEquals(time, "5:00 PM")
    }

    @Test
    fun check_24_to_12_conversion_235am() {
        val dateTime = HNLDateTime.newInstance()
        val time = dateTime.convert24HourMinuteTo12(2,35)
        assertEquals(time, "2:35 AM")
    }

    @Test
    fun check_12_noon() {
        val dateTime = HNLDateTime.newInstance()
        val time = dateTime.convert24HourMinuteTo12(12,0)
        assertEquals(time, "12:00 PM")
    }

    @Test
    fun check_midnight() {
        val dateTime = HNLDateTime.newInstance()
        val time = dateTime.convert24HourMinuteTo12(24,0)
        assertEquals(time, "12:00 AM")
    }

    @Test
    fun check_midnight_zero() {
        val dateTime = HNLDateTime.newInstance()
        val time = dateTime.convert24HourMinuteTo12(0,0)
        assertEquals(time, "12:00 AM")
    }
}
