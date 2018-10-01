package hackernewslater.williamha.com.hackernewslater.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.IGNORE
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import hackernewslater.williamha.com.hackernewslater.model.NewsItem

/**
 * Created by williamha on 6/18/18.
 */

@Dao
interface SavedNewsItemDao {

        @Query("SELECT * FROM NewsItem")
        fun loadAll(): List<NewsItem>

        @Query("SELECT * FROM NewsItem WHERE hackerNewsItemId = :newsItemId")
        fun loadNewsItem(newsItemId: Long): NewsItem

        // This is an asynchronous version that returns a LiveData wrapper around a single news item.
        @Query("SELECT * FROM NewsItem WHERE hackerNewsItemId = :newsItemId")
        fun loadLiveNewsItem(newsItemId: Long): LiveData<NewsItem>

        @Query("SELECT * FROM NewsItem WHERE hackerNewsItemId IN (:hackerNewsItemIds)")
        fun loadNewsItemsWithIds(hackerNewsItemIds: List<Long>): List<NewsItem>

        @Query("SELECT * FROM NewsItem WHERE isSaved = 1")
        fun loadSavedNewsItems(): List<NewsItem>

        @Query("SELECT * FROM NewsItem WHERE type LIKE :type ORDER BY time DESC LIMIT 300")
        fun loadMostRecentSetOfItems(type: String): List<NewsItem>

        @Query("SELECT * FROM NewsItem WHERE currentlyQueued = 1 AND isSaved = 1")
        fun returnCurrentlyQueuedItems(): List<NewsItem>

        @Query("SELECT * FROM NewsItem WHERE addedToQueue = 1")
        fun loadNewsItemsAddedToQueue(): List<NewsItem>

        @Query("SELECT * FROM NewsItem WHERE addedToQueue = 0 AND isSaved = 1")
        fun loadNewsItemsSavedButNotYetAddedToQueue(): List<NewsItem>

        @Query("SELECT * FROM NewsItem WHERE addedToQueue = 1 AND isSaved = 0")
        fun loadNewsItemsAddedToQueueButUnSaved(): List<NewsItem>

        @Insert(onConflict = REPLACE)
        fun insertNewsItem(newsItem: NewsItem): Long

        @Update(onConflict = REPLACE)
        fun updateNewsItem(newsItem: NewsItem)

        @Delete
        fun deleteNewsItem(newsItem: NewsItem)

        @Query("DELETE FROM NewsItem")
        fun deleteAllItems(): Int
}