package hackernewslater.williamha.com.hackernewslater.model

import android.arch.persistence.room.*

/**
 * Created by williamha on 6/14/18.
 */

@Entity
data class NewsItem(
        @PrimaryKey(autoGenerate = true)
        var id: Long? = null,
        var by: String? = null,
        var title: String? = null,
        var url: String = "",
        var time: Long = 0,
        var hackerNewsItemId: Long = 0,
        var isSaved: Boolean = false,
        var addedToQueue: Boolean = false,
        var currentlyQueued: Boolean = false,

        @TypeConverters(KidsConverter::class)
        @ColumnInfo(name = "kids") var kids: List<Long>? = null,

        var text: String = "",
        var type: String = ""
)

data class Kids(
    val kids: List<Long> = ArrayList()
)

class KidsConverter {

    @TypeConverter
    fun toKids(value: String?): List<Long> {
        if (value == null || value.isEmpty()) {
            return ArrayList()
        }

        val list: List<String> = value.split(",")

        val longList = ArrayList<Long>()

        for (item in list) {
            if (!item.isEmpty()) {
                longList.add(item.toLong())
            }
        }
        return longList
    }

    @TypeConverter
    fun toString(kids: List<Long>?): String {
        var string = ""

        if (kids == null) {
            return string
        }

        kids.forEach {
            string += "$it,"
        }
        return string
    }
}