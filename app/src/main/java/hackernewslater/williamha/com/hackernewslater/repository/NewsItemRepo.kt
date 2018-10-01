package hackernewslater.williamha.com.hackernewslater.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import hackernewslater.williamha.com.hackernewslater.db.SavedNewsItemDao
import hackernewslater.williamha.com.hackernewslater.db.NewsItemDatabase
import hackernewslater.williamha.com.hackernewslater.model.NewsItem
import hackernewslater.williamha.com.hackernewslater.model.Resource

/**
 * Created by williamha on 6/21/18.
 */

class NewsItemRepo(private val context: Context) {
    private var db: NewsItemDatabase = NewsItemDatabase.getInstance(context)
    private var savedNewsItemDao: SavedNewsItemDao = db.newsItemDao()

    fun addNewsItem(newsItem: NewsItem) {
        savedNewsItemDao.insertNewsItem(newsItem)
    }

    fun deleteNewsItem(newsItem: NewsItem) {
        savedNewsItemDao.deleteNewsItem(newsItem)
    }

    fun returnNewsItemWithId(id: Long): NewsItem? {
        return savedNewsItemDao.loadNewsItem(id)
    }

    fun returnLiveNewsItem(id: Long): LiveData<NewsItem>? {
        return savedNewsItemDao.loadLiveNewsItem(id)
    }

    fun updateNewsItem(newsItem: NewsItem) {
        savedNewsItemDao.updateNewsItem(newsItem)
    }

    fun returnNewsItemsWithIds(ids: List<Long>): List<NewsItem>? {
        return savedNewsItemDao.loadNewsItemsWithIds(ids)
    }

    fun returnSavedNewsItems(): List<NewsItem>? {
        return savedNewsItemDao.loadSavedNewsItems()
    }

    fun returnMostRecentItems(type: String): List<NewsItem> {
        return savedNewsItemDao.loadMostRecentSetOfItems(type)
    }

    fun returnCurrentlyQueuedItems(): List<NewsItem> {
        val items = savedNewsItemDao.returnCurrentlyQueuedItems()
        return items.distinctBy { it.hackerNewsItemId }
    }

    fun returnItemsAddedToQueueButUnsaved(): List<NewsItem>? {
        return savedNewsItemDao.loadNewsItemsAddedToQueueButUnSaved()
    }

    fun returnSavedNewsItemsButNotQueued(): List<NewsItem>? {
        return savedNewsItemDao.loadNewsItemsSavedButNotYetAddedToQueue()
    }

    fun deleteAllItems() {
        val itemsDeleted = savedNewsItemDao.deleteAllItems()
        Log.d("TAG", itemsDeleted.toString())
    }

    val allNewsItems: List<NewsItem>
        get() {
            return savedNewsItemDao.loadAll()
        }
}