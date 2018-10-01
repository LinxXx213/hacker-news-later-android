package hackernewslater.williamha.com.hackernewslater.service

import hackernewslater.williamha.com.hackernewslater.BuildConfig
import hackernewslater.williamha.com.hackernewslater.HNL_BASE_URL_DEBUG
import hackernewslater.williamha.com.hackernewslater.managers.HNLAccountManager
import hackernewslater.williamha.com.hackernewslater.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

/**
 * Created by williamha on 7/11/18.
 */
interface HackerNewsApiService {

    // Accounts

    @POST("/accounts/sign-up")
    fun register(@Body emailPassword: EmailPassword): Call<HNLAccountManager.UserStatus>

    @POST("/accounts/google-oauth")
    fun registerLoginWithGoogle(@Body token: GoogleToken): Call<HNLAccountManager.UserStatus>

    @POST("/login")
    fun login(@Body emailPassword: EmailPassword): Call<ResponseBody>

    @GET("/accounts/reset-password")
    fun requestPasswordReset(@Query("email") email: String): Call<ResponseBody>

    @POST("/accounts/reset-password")
    fun requestPasswordWithCredentials(@Body resetPassword: ResetPassword): Call<ResponseBody>

    @POST("/accounts/update-settings")
    fun updateAccountWithSettings(@Header("Authorization") authorization: String,
                                  @Body userDetails: HNLAccountManager.UserDetails): Call<ResponseBody>

    @GET("/accounts/account-details")
    fun getUserAccountDetails(@Header("Authorization") authorization: String): Call<UserAccountResponse>

    // NewsItems

    @POST("/add-item")
    fun addItem(@Header("Authorization") authorization: String, @Body newsItem: NewsItem): Call<ResponseBody>

    @POST("/add-items")
    fun addItems(@Header("Authorization") authorization: String, @Body newsItem: List<NewsItem>): Call<ResponseBody>

    @DELETE("/delete-item")
    fun deleteItem(@Header("Authorization") authorization: String, @Query("id") hackerNewsItemid: Long): Call<ResponseBody>

    @POST("/delete-items")
    fun deleteItems(@Header("Authorization") authorization: String, @Body newsItem: List<NewsItem>): Call<ResponseBody>

    @DELETE("/delete-all")
    fun deleteAllItems(@Header("Authorization") authorization: String): Call<ResponseBody>

    @GET("/queued-items")
    fun queuedItems(@Header("Authorization") authorization: String): Call<UserAccountQueuedItemsResponse>

    @FormUrlEncoded
    @POST("/read-now")
    fun readNow(@Header("Authorization") authorization: String, @Field("url") url: String): Call<ResponseBody>

    // Payment
    @POST("/payment/charge")
    fun charge(@Header("Authorization") authorization: String, @Body token: StripeToken): Call<UserAccountResponse>

    companion object {
        val instance: HackerNewsApiService by lazy {
            var baseUrl = "https://lit-eyrie-32886.herokuapp.com/"
            if (BuildConfig.DEBUG) {
                baseUrl = HNL_BASE_URL_DEBUG
            }

            val retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            retrofit.create<HackerNewsApiService>(HackerNewsApiService::class.java)
        }
    }

    data class VerifyEmail(val email: String, val token: String)
    data class ResetPassword(val email: String, val password: String, val resetToken: String)
    data class GoogleToken(val token: String)
}