package hackernewslater.williamha.com.hackernewslater.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import hackernewslater.williamha.com.hackernewslater.exceptions.HNLException
import hackernewslater.williamha.com.hackernewslater.model.NewsItem
import hackernewslater.williamha.com.hackernewslater.model.Resource
import hackernewslater.williamha.com.hackernewslater.repository.HackerNewsApiRepo
import hackernewslater.williamha.com.hackernewslater.repository.HackerNewsRepo
import hackernewslater.williamha.com.hackernewslater.repository.NewsItemRepo
import hackernewslater.williamha.com.hackernewslater.service.HackerNewsApiService
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineExceptionHandler
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

/**
 * Created by williamha on 6/13/18.
 */

class NewsItemViewModel(application: Application): AndroidViewModel(application) {

    var newsItemRepo: NewsItemRepo = NewsItemRepo(getApplication())
    var hackerNewsApiRepo = HackerNewsApiRepo(getApplication(), HackerNewsApiService.instance)
    var hackerNewsRepo: HackerNewsRepo? = null


    data class NewsItemResponse(val id: Long,
                                val by: String?,
                                val title: String?,
                                val url: String?,
                                val time: Long,
                                var isSaved: Boolean,
                                var kids: List<Long>?,
                                val text: String?,
                                val type: String?)

    fun returnNewsItems(type: NewsItemType, isOffline: Boolean, topFirstEntries: Int = 100): LiveData<Resource<List<NewsItem>>> {

        val newsItems = MutableLiveData<Resource<List<NewsItem>>>()
        val repo = hackerNewsRepo

        when(type) {
            NewsItemType.TOP ->  {

                if (isOffline) {
                    launch(CommonPool) {
                        val offlineItems = returnItemsForOfflineMode()

                        launch(UI) {
                            newsItems.value = offlineItems
                        }
                    }
                } else {
                    repo?.searchTopNews { results, exception ->
                        exception?.let {
                            newsItems.value = Resource.error(it)
                        }

                        if (results != null) {
                            // To prevent out of bounds
                            val capTopFirstEntries = Math.min(results.size, topFirstEntries)

                            // Get a list of ids that are on the top, make a sublist limited to the specified amount
                            val topList = results.subList(0, capTopFirstEntries)
                            val itemsToReturn = mutableListOf<NewsItem>()

                            for (item in topList) {
                                repo.getNewsItemDetails(item, { fetchedItem ->
                                    itemsToReturn.add(newsItemResponseToNewsItem(fetchedItem))

                                    if (itemsToReturn.size % 10 == 0) {
                                        newsItems.value = Resource.success(itemsToReturn)
                                    }
                                }, false)
                            }
                        }
                    }
                }
            }

            NewsItemType.QUEUED -> {
                if (isOffline) {
                    launch(CommonPool) {
                        val offlineQueuedItems = returnCurrentlyQueuedItems()
                        launch(UI) {
                            newsItems.value = offlineQueuedItems
                        }
                    }
                } else {
                    returnCurrentlyQueuedItems { items, exception ->
                        exception?.let {
                            newsItems.value = Resource.error(it)
                        }
                        newsItems.value = Resource.success(items)
                    }
                }
            }
        }

        return newsItems
    }

    fun returnCurrentlyQueuedItems(callback:(savedItems:List<NewsItem>, exception: HNLException?) -> Unit) {
        hackerNewsApiRepo.returnQueuedItems { success, exception, items ->
            if (success && exception == null) {

                launch(CommonPool + CoroutineExceptionHandler({ _, e ->
                    Log.e("TAG", "CoroutineExceptionHandler", e)
                })) {
                    val items = newsItemRepo.returnNewsItemsWithIds(items)

                    items?.let { existingItems ->
                        val uniqueItems = existingItems.distinctBy { it.hackerNewsItemId }.map {
                            it.currentlyQueued = true
                            it.addedToQueue = true
                            it.isSaved = true
                            newsItemRepo.updateNewsItem(it)
                            it
                        }

                        launch(UI) {
                            callback(uniqueItems, null)
                        }
                    }
                }
            } else {
                callback(emptyList(), exception)
            }
        }
    }

    fun updateNewsItem(newsItem: NewsItem) {
        newsItemRepo.updateNewsItem(newsItem)
    }

    fun returnItemsForOfflineMode(): Resource<List<NewsItem>> {
        return Resource.success(newsItemRepo.returnMostRecentItems("story"))
    }

    fun returnCurrentlyQueuedItems(): Resource<List<NewsItem>> {
        return Resource.success(newsItemRepo.returnCurrentlyQueuedItems())
    }

    fun returnItemWithId(hackerNewsItemId: Long): NewsItem? {
        return newsItemRepo.returnNewsItemWithId(hackerNewsItemId)
    }

    fun returnLiveNewsItem(hackerNewsItemId: Long): LiveData<NewsItem>? {
        return newsItemRepo.returnLiveNewsItem(hackerNewsItemId)
    }

    fun returnAllSavedItems(): List<NewsItem>? {
        return newsItemRepo.returnSavedNewsItems()
    }

    companion object {
        fun newsItemResponseToNewsItem(newsItemResponse: NewsItemResponse): NewsItem {
            var newsItem = NewsItem()
            val newsItemResponseId = newsItemResponse.id
            if (newsItemResponse.url == null) {
                newsItem.url =  "https://news.ycombinator.com/item?id=%d".format(newsItemResponseId)
            } else {
                newsItemResponse.url.let { newsItem.url = it }
            }

            newsItem.by = newsItemResponse.by

            newsItemResponse.title?.let {
                newsItem.title = it
            }

            newsItem.isSaved = newsItemResponse.isSaved
            newsItem.time = newsItemResponse.time
            newsItem.hackerNewsItemId = newsItemResponse.id
            newsItem.kids = newsItemResponse.kids

            newsItemResponse.text?.let {
                newsItem.text = it
            }

            newsItemResponse.type?.let {
                newsItem.type = it
            }

            return newsItem
        }

        fun newsItemToNewsItemResponse(newsItem: NewsItem): NewsItemViewModel.NewsItemResponse {
            return NewsItemViewModel.NewsItemResponse(newsItem.hackerNewsItemId,
                    newsItem.by,
                    newsItem.title,
                    newsItem.url,
                    newsItem.time,
                    newsItem.isSaved,
                    newsItem.kids,
                    newsItem.text,
                    newsItem.type)
        }
    }

    enum class NewsItemType {
        TOP, QUEUED
    }
}