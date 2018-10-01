package hackernewslater.williamha.com.hackernewslater.ui.utilities

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by williamha on 8/1/18.
 */
class HNLDateTime {

    fun convertHourToGMT(localHour: Int): Int {
        var calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, localHour)
        calendar.timeZone = TimeZone.getDefault()

        val calendarDate = calendar.time

        val hourFormatter = SimpleDateFormat("HH")
        hourFormatter.timeZone = TimeZone.getTimeZone("UTC")

        return Integer.valueOf(hourFormatter.format(calendarDate))
    }

    fun convertGMTToLocalHour(gmtHour: Int): Int {
        var calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, gmtHour)
        calendar.timeZone = TimeZone.getTimeZone("UTC")

        val calendarDate = calendar.time

        val hourFormatter = SimpleDateFormat("HH")
        hourFormatter.timeZone = TimeZone.getDefault()

        return Integer.valueOf(hourFormatter.format(calendarDate))
    }

    fun convert24HourMinuteTo12(hour: Int, minute: Int): String {
        val time = Calendar.getInstance()

        time.set(Calendar.HOUR_OF_DAY, hour)
        time.set(Calendar.MINUTE, minute)

        val minuteString = "%02d".format(time.get(Calendar.MINUTE))

        var ampmString = "PM"
        if (time.get(Calendar.AM_PM) == 0) ampmString = "AM"

        var hourString = "12"
        if (time.get(Calendar.HOUR) != 0) hourString = time.get(Calendar.HOUR).toString()

        return "%s:%s %s".format(hourString, minuteString, ampmString)
    }

    fun xmlDateToDate(date: String?): Date {
        val date = date?: return Date()
        val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z")
        return format.parse(date)
    }

    fun isNextday(date: Date): Boolean {
        val twentyFourHourSeconds = 10000
//        val twentyFourHourSeconds = TimeUnit.HOURS.toMillis(24)
        val currentTime = Date().time
        if (currentTime - date.time > twentyFourHourSeconds) {
            return true
        }
        return false
    }

    fun isNextWeek(date: Date): Boolean {
        val oneWeek = TimeUnit.DAYS.toMillis(7)
        val currentTime = Date().time
        if (currentTime - date.time > oneWeek) {
            return true
        }
        return false
    }

    companion object {
        fun newInstance(): HNLDateTime {
            return HNLDateTime()
        }
    }
}