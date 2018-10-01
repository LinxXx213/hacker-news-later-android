package hackernewslater.williamha.com.hackernewslater.repository

import android.content.Context
import android.util.Log
import hackernewslater.williamha.com.hackernewslater.exceptions.HNLException
import hackernewslater.williamha.com.hackernewslater.service.HackerNewsService
import hackernewslater.williamha.com.hackernewslater.viewmodel.NewsItemViewModel
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineExceptionHandler
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by williamha on 5/25/18.
 */
class HackerNewsRepo(private val context: Context, private val hnService: HackerNewsService) {

    val TAG = javaClass.simpleName

    private var newsItemRepo: NewsItemRepo = NewsItemRepo(context)

    fun searchTopNews(callBack:(List<Long>?, exception: HNLException?) -> Unit) {
        val hnTopCall = hnService.getTopStories()

        hnTopCall.enqueue(object: Callback<List<Long>> {
            override fun onFailure(call: Call<List<Long>>?, t: Throwable?) {
                var exception = HNLException(t.toString())
                callBack(null, exception)
            }

            override fun onResponse(call: Call<List<Long>>?, response: Response<List<Long>>?) {
                Log.i(TAG, "Got response with status code " +
                        "${response?.code()} and message " +
                        "${response?.message()}")

                val body = response?.body()
                body?.let {
                    callBack(it, null)
                }
            }
        })
    }

    fun getNewsItemDetails(id: Long, callback: (NewsItemViewModel.NewsItemResponse) -> Unit, forceRefresh: Boolean) {
        if (forceRefresh) {
            fetchItemDetail(id, callback)
        } else {
            launch(CommonPool + CoroutineExceptionHandler({ _, e ->

                Log.e("TAG", "CoroutineExceptionHandler", e)

            })) {
                Log.d(TAG, "Fetching data from store")
                var itemFromStore = newsItemRepo.returnNewsItemWithId(id)

                if (itemFromStore != null) {
                    val newsItemReponse = NewsItemViewModel.newsItemToNewsItemResponse(itemFromStore)

                    launch(UI) {
                        callback(newsItemReponse)
                    }

                } else {
                    fetchItemDetail(id, callback)
                }
            }
        }
    }

    private fun fetchItemDetail(id: Long, callback:(NewsItemViewModel.NewsItemResponse) -> Unit) {

        val hnNewsItemCall = hnService.getNewsItemDetails(id)

        hnNewsItemCall.enqueue(object : Callback<NewsItemViewModel.NewsItemResponse> {
            override fun onFailure(call: Call<NewsItemViewModel.NewsItemResponse>?, t: Throwable?) {
                Log.i(TAG, "Call to ${call?.request()?.url()} " +
                        "failed with ${t.toString()}")

                hnNewsItemCall.cancel()
            }

            override fun onResponse(call: Call<NewsItemViewModel.NewsItemResponse>?, response: Response<NewsItemViewModel.NewsItemResponse>?) {
                Log.i(TAG, "Got response with status code " +
                        "${response?.code()} and message " +
                        "${response?.message()}")

                val body = response?.body()
                body?.let {
                    val newsItem = NewsItemViewModel.newsItemResponseToNewsItem(it)
                    launch(CommonPool) {
                        newsItemRepo.addNewsItem(newsItem)
                    }

                    Log.d(TAG, "Fetching data from the internet, calling back item.")
                    callback(it)
                }
            }
        })
    }
}