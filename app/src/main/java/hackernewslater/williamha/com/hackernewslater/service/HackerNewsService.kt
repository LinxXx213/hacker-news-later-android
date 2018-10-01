package hackernewslater.williamha.com.hackernewslater.service

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import hackernewslater.williamha.com.hackernewslater.model.Kids
import hackernewslater.williamha.com.hackernewslater.model.KidsConverter
import hackernewslater.williamha.com.hackernewslater.viewmodel.NewsItemViewModel
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.lang.reflect.Type

/**
 * Created by williamha on 5/25/18.
 */
interface HackerNewsService {
    @GET("topstories.json")
    fun getTopStories(): Call<List<Long>>

    @GET("item/{id}.json")
    fun getNewsItemDetails(@Path("id") id: Long): Call<NewsItemViewModel.NewsItemResponse>

    companion object {
        val instance: HackerNewsService by lazy {
            val retrofit = Retrofit.Builder()
                    .baseUrl("https://hacker-news.firebaseio.com/v0/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            retrofit.create<HackerNewsService>(HackerNewsService::class.java)
        }
    }


}