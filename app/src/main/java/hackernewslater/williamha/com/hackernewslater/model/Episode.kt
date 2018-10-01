package hackernewslater.williamha.com.hackernewslater.model

import android.arch.persistence.room.*
import java.util.*

/**
 * Created by williamha on 9/3/18.
 */
@Entity
data class Episode(
        @PrimaryKey var guid:String = "",
        var podcastId:Long? = null,
        var title:String = "",
        var description:String = "",
        var mediaUrl:String = "",
        var mimeType:String = "",
        var releaseDate:Date = Date(),
        var duration:String = ""
)

class EpisodeConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun toTimeStamp(date: Date?): Long? {
        return (date?.time)
    }
}