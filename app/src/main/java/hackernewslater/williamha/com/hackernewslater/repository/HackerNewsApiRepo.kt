package hackernewslater.williamha.com.hackernewslater.repository

import android.content.Context
import android.util.Log
import hackernewslater.williamha.com.hackernewslater.R
import hackernewslater.williamha.com.hackernewslater.exceptions.HNLAPIError
import hackernewslater.williamha.com.hackernewslater.exceptions.HNLException
import hackernewslater.williamha.com.hackernewslater.managers.HNLAccountManager
import hackernewslater.williamha.com.hackernewslater.model.*
import hackernewslater.williamha.com.hackernewslater.service.HackerNewsApiService
import okhttp3.Request

import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by williamha on 7/11/18.
 */
class HackerNewsApiRepo(val context: Context, val hnApiService:HackerNewsApiService) {

    val token = HNLAccountManager.getInstance(context).getToken()

    // Accounts

    fun loginWithUserAccount(user: EmailPassword, callback:(success: Boolean, token: String?, exception: HNLException?) -> Unit) {
        val loginCall = hnApiService.login(user)

        makeHttpRequestForCall(loginCall) { response, exception ->
            val statusCode = response?.code()
            statusCode.let { statusCode ->
                // Grab and inspect the header
                val headers = response?.headers()
                headers?.get("Authorization").let { token ->
                    callback(statusCode == 200, token.toString(), exception)
                }
            }
        }
    }

    fun registerUser(user: EmailPassword, callback:(success: Boolean, userStatus: HNLAccountManager.UserStatus?, token: String?, exception: HNLException?) -> Unit) {
        val registerCall = hnApiService.register(user)
        registerCallForRegistrationLogin(registerCall, callback)
    }

    fun registerWithGoogle(googleIdToken: String, callback:(success: Boolean, userStatus: HNLAccountManager.UserStatus?, token: String?, exception: HNLException?) -> Unit) {
        val registerLoginWithGoogleCall = hnApiService.registerLoginWithGoogle(HackerNewsApiService.GoogleToken(googleIdToken))
        registerCallForRegistrationLogin(registerLoginWithGoogleCall, callback)
    }

    fun registerCallForRegistrationLogin(call: Call<HNLAccountManager.UserStatus>, callback:(success: Boolean, userStatus: HNLAccountManager.UserStatus?, token: String?, exception: HNLException?) -> Unit) {
        call.enqueue(object: Callback<HNLAccountManager.UserStatus> {

            override fun onResponse(call: Call<HNLAccountManager.UserStatus>?, response: Response<HNLAccountManager.UserStatus>?) {
                response.let {
                    val statusCode = response?.code()
                    val headers = response?.headers()
                    var token: String? = null
                    headers?.get("Authorization").let { token = it.toString() }

                    val error = handleError(response?.errorBody())
                    var exception: HNLException? = null
                    error?.let {
                        exception = HNLException(it.errorMessage)
                    }

                    callback(statusCode == 200, it?.body(), token, exception)
                }
            }

            override fun onFailure(call: Call<HNLAccountManager.UserStatus>?, t: Throwable?) {
                t?.let {
                    callback(false, null, null, HNLException(it.localizedMessage))
                }
            }
        })
    }


    fun requestPasswordReset(email: String, callback:(success: Boolean, exception: HNLException?) -> Unit) {
        val requestPasswordResetCall = hnApiService.requestPasswordReset(email)
        makeHttpRequestForCall(requestPasswordResetCall) { response, exception ->
            response.let {
                val statusCode = response?.code()
                statusCode.let {
                    callback(it == 200, exception)
                }
            }
        }
    }

    fun updateAccountWithSettings(userDetails: HNLAccountManager.UserDetails, callback:(success: Boolean, exception: HNLException?) -> Unit) {
        val requestUpdateSettings = hnApiService.updateAccountWithSettings(token, userDetails)
        makeHttpRequestForCall(requestUpdateSettings) { response, exception ->
            response.let {
                val statusCode = response?.code()
                statusCode.let {
                    callback(it == 200, exception)
                }
            }
        }
    }

    fun accountDetails(callback:(userDetails: UserAccount?, exception: HNLException?) -> Unit) {
        val requestGetUserDetails = hnApiService.getUserAccountDetails(token)

        requestGetUserDetails.enqueue(object : Callback<UserAccountResponse> {
            override fun onResponse(call: Call<UserAccountResponse>?, response: Response<UserAccountResponse>?) {
                response?.let { response ->
                    val userAccount = response.body()
                    userAccount?.let { userAccount ->
                        val userAccount = userAccount.userAccountResponseToUserAccount()
                        callback(userAccount, null)
                    }
                }
            }

            override fun onFailure(call: Call<UserAccountResponse>?, t: Throwable?) {
                t?.let {
                    val message = it.localizedMessage
                    callback(null, HNLException(message))
                }
            }
        })
    }


    // NewsItems

    fun addItem(newsItem: NewsItem, callback:(success: Boolean, exception: HNLException?) -> Unit) {
        val requestAddNewsItem = hnApiService.addItem(token, newsItem)
        makeHttpRequestForCall(requestAddNewsItem) { response, exception ->

            var localException = exception
            response.let {
                val statusCode = response?.code()
                statusCode.let {
                    val error = handleError(response?.errorBody())
                    error?.let { localException = HNLException(it.errorMessage) }
                    callback(it == 200, localException)
                }
            }
        }
    }

    fun addItems(newsItem: List<NewsItem>, callback:(success: Boolean, exception: HNLException?) -> Unit) {
        val requestAddNewsItem = hnApiService.addItems(token, newsItem)
        makeHttpRequestForCall(requestAddNewsItem) { response, exception ->

            var localException = exception
            response.let {
                val statusCode = response?.code()
                statusCode.let {

                    if (it != 200) {
                        val error = handleError(response?.errorBody())
                        error?.let {
                            localException = HNLException(it.errorMessage)
                        }
                    }
                    callback(it == 200, localException)
                }
            }
        }
    }

    fun deleteNewsItem(newsItem: NewsItem, callback:(success: Boolean, exception: HNLException?) -> Unit) {
        val requestDeleteNewsItem = hnApiService.deleteItem(token, newsItem.hackerNewsItemId)
        makeHttpRequestForCall(requestDeleteNewsItem) { response, exception ->
            response.let {
                val statusCode = response?.code()
                statusCode.let { callback(it == 200, exception) }
            }
        }
    }

    fun deleteNewsItems(newsItems: List<NewsItem>, callback:(success: Boolean, exception: HNLException?) -> Unit) {
        val requestDeleteNewsItem = hnApiService.deleteItems(token, newsItems)
        makeHttpRequestForCall(requestDeleteNewsItem) { response, exception ->
            response.let {
                val statusCode = response?.code()
                statusCode.let { callback(it == 200, exception) }
            }
        }
    }

    fun returnQueuedItems(callback:(success: Boolean, exception: HNLException?, items: List<Long>) -> Unit) {
        val requestQueuedItems = hnApiService.queuedItems(token)
        requestQueuedItems.enqueue(object: Callback<UserAccountQueuedItemsResponse> {

            override fun onResponse(call: Call<UserAccountQueuedItemsResponse>?, response: Response<UserAccountQueuedItemsResponse>?) {
                response.let {
                    val statusCode = response?.code()
                    statusCode.let {
                        if (it == 200) {
                            response?.body()?.let { responseBody ->
                                val items = responseBody.items
                                callback(it == 200, null, items)
                            }
                        } else {
                            when (it) {
                                403 -> {
                                    callback(it == 200, HNLException("Please sign in to view your queued items, if any."), emptyList())
                                }

                                else -> {
                                    callback(it == 200, HNLException("There was an error getting your queued items. Please try again."), emptyList())
                                }
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<UserAccountQueuedItemsResponse>?, t: Throwable?) {
                t?.let {
                    callback(false, HNLException(it.localizedMessage), emptyList())
                }
            }
        })
    }


    fun deleteAllItems(callback:(success: Boolean, exception: HNLException?) -> Unit) {
        val requestDeleteAllItems = hnApiService.deleteAllItems(token)
        makeHttpRequestForCall(requestDeleteAllItems) { response, exception ->
            response.let {
                val statusCode = response?.code()
                statusCode.let { callback(it == 200, exception) }
            }
        }
    }

    fun decrementNumberOfReads(url: String, callback:(success: Boolean, exception: HNLException?) -> Unit) {
        val readNowRequest = hnApiService.readNow(token, url)
        makeHttpRequestForCall(readNowRequest) { response, exception ->
            response.let {
                val statusCode = response?.code()
                statusCode.let { callback(it == 200, exception) }
            }
        }
    }

    // Payment

    fun chargeUser(stripeToken: StripeToken, callback:(user: UserAccountResponse?, exception: HNLException?) -> Unit) {
        val chargeRequest = hnApiService.charge(token, stripeToken)

        chargeRequest.enqueue(object: Callback<UserAccountResponse> {
            override fun onResponse(call: Call<UserAccountResponse>?, response: Response<UserAccountResponse>?) {
                response?.let {
                    response?.let { response ->
                        val error = handleError(response.errorBody())
                        var exception: HNLException? = null
                        error?.let { exception = HNLException(it.errorMessage) }
                        val body = it.body()
                        callback(body, exception)
                    }
                }
            }

            override fun onFailure(call: Call<UserAccountResponse>?, t: Throwable?) {
                var exception = HNLException("An error occurred on your request.")
                t?.let { error->
                    error.message?.let { message ->
                        exception = HNLException(message)

                    }
                }
                callback(null, exception)
            }
        })
    }

    // Helpers

    private fun makeHttpRequestForCall(requestCall: Call<ResponseBody>, callback:(response: Response<ResponseBody>?, exception: HNLException?) -> Unit) {
        requestCall.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                response?.let { response ->
                    var exception: HNLException? = null
                    val statusCode = response.code()
                    if (statusCode == 401 || statusCode == 403) {
                        exception = HNLException("You are not authorized. Please log in and try again.")

                        // Force log out
                        HNLAccountManager.getInstance(context).logout()
                    } else if (statusCode == 502) {
                        val serverDownMessage = context.resources.getString(R.string.server_down)
                        exception = HNLException(serverDownMessage)
                    }
                    callback(response, exception)
                }
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                callback(null, HNLException("An error occurred on your request. The internet may be down."))
            }
        })
    }

    private fun handleError(errorBody: ResponseBody?): HNLAPIError? {
        var error = HNLAPIError("", "An error occurred on your request.")
        if (errorBody == null) {
            return null
        }

        val errorBodyString = errorBody.string()
        if (errorBodyString.isEmpty()) return null

        val jsonObject = JSONObject(errorBodyString)

        jsonObject?.let {
            try {
                val errorMessage = it.get("errorMessage")
                val errorCode = it.get("errorCode")
                error.errorMessage = errorMessage.toString()
                error.errorCode = errorCode.toString()
            } catch (ex: Exception) {
                try {
                    val errorMessage = it.get("message")
                    error.errorMessage = errorMessage.toString()
                } catch (ex: Exception) {
                    return error
                }
            }
        }
        return error
    }
}
