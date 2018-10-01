package hackernewslater.williamha.com.hackernewslater.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import hackernewslater.williamha.com.hackernewslater.model.*

/**
 * Created by williamha on 6/21/18.
 */

@Database(entities = arrayOf(NewsItem::class, Podcast::class, Episode::class), version = 11)
@TypeConverters(KidsConverter::class, EpisodeConverter::class, EpisodesConverter::class)
abstract class NewsItemDatabase : RoomDatabase() {
// database must be abstract to inherit from RoomDatabase

    abstract fun newsItemDao(): SavedNewsItemDao
    abstract fun podcastDao(): PodcastDao

    companion object {

        var TEST_MODE = false

        private var instance: NewsItemDatabase? = null
            fun getInstance(context: Context): NewsItemDatabase {
                if (instance == null) {
                    if (TEST_MODE) {
                        instance = Room.inMemoryDatabaseBuilder(context, NewsItemDatabase::class.java).allowMainThreadQueries().build()
                    } else {
                        instance = Room.databaseBuilder(
                                context.applicationContext,
                                NewsItemDatabase::class.java,
                                "SavedNewsItem")
                                .fallbackToDestructiveMigration()
                                .build()
                    }
                }
                return instance as NewsItemDatabase
            }
    }
}