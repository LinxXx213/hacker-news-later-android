package hackernewslater.williamha.com.hackernewslater.model

import android.arch.persistence.room.*
import com.google.gson.Gson
import java.util.*

/**
 * Created by williamha on 9/3/18.
 */

@Entity
data class Podcast(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    var feedUrl: String = "",
    var title: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var lastUpdated: Date = Date()
)

class EpisodesConverter {

    @TypeConverter
    fun listToJson(value: List<Episode>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToList(value: String): List<Episode>? {
        val objects = Gson().fromJson(value, Array<Episode>::class.java) as Array<Episode>
        val list = objects.toList()
        return list
    }
}