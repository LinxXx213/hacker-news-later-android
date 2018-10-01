package hackernewslater.williamha.com.hackernewslater.managers

import android.content.Context
import android.content.Intent
import android.provider.Settings.Global.getString
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import hackernewslater.williamha.com.hackernewslater.R
import hackernewslater.williamha.com.hackernewslater.SingletonHolder
import hackernewslater.williamha.com.hackernewslater.USER_AUTH_STATE_CHANGED
import hackernewslater.williamha.com.hackernewslater.exceptions.HNLException
import hackernewslater.williamha.com.hackernewslater.model.EmailPassword
import hackernewslater.williamha.com.hackernewslater.model.TimeToSend
import hackernewslater.williamha.com.hackernewslater.model.UserAccount
import hackernewslater.williamha.com.hackernewslater.repository.HackerNewsApiRepo
import hackernewslater.williamha.com.hackernewslater.repository.NewsItemRepo
import hackernewslater.williamha.com.hackernewslater.service.HackerNewsApiService
import hackernewslater.williamha.com.hackernewslater.ui.MainActivity
import hackernewslater.williamha.com.hackernewslater.ui.utilities.HNLDateTime
import hackernewslater.williamha.com.hackernewslater.ui.utilities.Toasty
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

/**
 * Created by williamha on 7/12/18.
 */

class HNLAccountManager private constructor(val context: Context) {

    val PREFS_FILE = "com.williamha.hackernewslater.prefs"
    val TOKEN = "token"
    val HOUR = "hour"
    val MINUTE = "minute"
    val MAX_ITEM_TO_SEND = "max_send"
    val MAX_ITEM_TO_SHOW = "max_show"
    val USER_STATUS = "user_status"

    fun isLoggedIn(): Boolean {
        val token = getToken()
        return !token.isEmpty()
    }

    fun logout() {
        saveToken(null)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        GoogleSignIn.getClient(context, gso).signOut()

        // Nuke table
        val newsItemRepo = NewsItemRepo(context)
        launch(CommonPool) {
            newsItemRepo.deleteAllItems()
        }

        // Send broadcast that user has logged out.
        val localBroadcastManager = LocalBroadcastManager.getInstance(context)
        val localIntent = Intent(USER_AUTH_STATE_CHANGED)
        localBroadcastManager.sendBroadcast(localIntent)
    }

    fun login(email: String, password: String, callback:(success: Boolean, exception: HNLException?) -> Unit) {
        val emailPassword = EmailPassword(email, password)
        val apiRepo = HackerNewsApiRepo(context, HackerNewsApiService.instance)
        apiRepo.loginWithUserAccount(emailPassword) { success, token, exception ->
            if (success) {
                token?.let { saveToken(it) }
                callback(success, exception)
            } else {
                val incorrectAuth = HNLException("The email or password provided is incorrect")
                callback(success, incorrectAuth)
            }

        }
    }

    fun handleSuccessfulLoginRegistration(isLogin: Boolean, callback:(loggedInMessage: String, exception: HNLException?) -> Unit) {
        val apiRepo = HackerNewsApiRepo(context, HackerNewsApiService.instance)
        apiRepo.accountDetails { userDetails: UserAccount?, exception: HNLException? ->
            userDetails?.let { userDetails ->

                // Add the items into the database from the server
                val newsItemRepo = NewsItemRepo(context)
                launch(CommonPool) {
                    userDetails.savedItems?.let { savedItems ->
                        for (item in savedItems) {
                            item.isSaved = true
                            item.addedToQueue = true
                            newsItemRepo.addNewsItem(item)
                        }
                    }
                }

                // Set the configurations

                // Be mindful of the time. From the server, this integer is UTC. Make the conversion before saving anything.
                val dateTime = HNLDateTime.newInstance()
                val timeToSend = TimeToSend(userDetails.timeToSend.hourToSend, userDetails.timeToSend.minuteToSend)
                setTimeToSend(timeToSend)
                setMaxItemsToSend(userDetails.maxItemToSend)

                var minute = timeToSend.minuteToSend
                val localHour = dateTime.convertGMTToLocalHour(timeToSend.hourToSend)
                val timeString = dateTime.convert24HourMinuteTo12(localHour, minute)
                val maxItemString = userDetails.maxItemToSend.toString()
                var loggedIn = "all set!"
                if (isLogin) loggedIn = "logged in!"

                val message = "You are now %s Any items queued is slated to send tomorrow at %s and the amount of items to send is %s. You can change these settings in the hamburger menu.".format(loggedIn, timeString, maxItemString)
                callback(message, exception)
            }
        }
    }

    fun getUserDetails(): UserDetails {
        val timeToSend = getTimeToSend()
        val maxItemToSend = getMaxItemsToSend()
        return UserDetails(timeToSend.hourToSend, timeToSend.minuteToSend, maxItemToSend)
    }

    fun updateUserSettings(callback: (success: Boolean, exception: HNLException?) -> Unit) {
        if (!isLoggedIn()) return
        val userDetails = getUserDetails()
        val apiRepo = HackerNewsApiRepo(context, HackerNewsApiService.instance)
        apiRepo.updateAccountWithSettings(userDetails) { success, exception ->
            callback(success, exception)
        }
    }

    fun register(email: String, password: String, callback:(success: Boolean, exception: HNLException?) -> Unit) {
        val emailPassword = EmailPassword(email, password)
        val apiRepo = HackerNewsApiRepo(context, HackerNewsApiService.instance)
        apiRepo.registerUser(emailPassword) { success, userStatus, token, exception ->
            userStatus?.let { setUserStatus(it.enabled) }
            token?.let { saveToken(it) }
            callback(success, exception)
        }
    }

    fun registerWithGoogle(googleIdToken: String, callback:(success: Boolean, userStatus: UserStatus?, exception: HNLException?) -> Unit) {
        val apiRepo = HackerNewsApiRepo(context, HackerNewsApiService.instance)
        apiRepo.registerWithGoogle(googleIdToken) { success, userStatus, token, exception ->
            userStatus?.let { setUserStatus(it.enabled) }
            token?.let { saveToken(it) }
            callback(success, userStatus, exception)
        }
    }

    fun forgotPassword(email: String, callback:() -> Unit) {
        val apiRepo = HackerNewsApiRepo(context, HackerNewsApiService.instance)
        apiRepo.requestPasswordReset(email) { _, _->
            callback()
        }
    }

    fun saveToken(token: String?) {
        val preferences = context.getSharedPreferences(PREFS_FILE, 0)
        val editor = preferences.edit()
        editor.putString(TOKEN, token)
        editor.apply()
    }

    fun getToken(): String {
        val preferences = context.getSharedPreferences(PREFS_FILE, 0)
        return preferences.getString(TOKEN, null) ?: ""
    }

    fun setTimeToSend(timeToSend: TimeToSend) {
        val preferences = context.getSharedPreferences(PREFS_FILE, 0)
        val editor = preferences.edit()
        editor.putInt(HOUR, timeToSend.hourToSend)
        editor.putInt(MINUTE, timeToSend.minuteToSend)
        editor.apply()
    }

    fun getTimeToSend(): TimeToSend {
        val preferences = context.getSharedPreferences(PREFS_FILE, 0)
        val hourToSend = preferences.getInt(HOUR, 12)
        val minuteToSend = preferences.getInt(MINUTE, 0)
        return TimeToSend(hourToSend, minuteToSend)
    }

    fun setMaxItemsToSend(maxNumber: Int) {
        val preferences = context.getSharedPreferences(PREFS_FILE, 0)
        val editor = preferences.edit()
        editor.putInt(MAX_ITEM_TO_SEND, maxNumber)
        editor.apply()
    }

    fun getMaxItemsToSend(): Int {
        val preferences = context.getSharedPreferences(PREFS_FILE, 0)
        val hourToSend = preferences.getInt(MAX_ITEM_TO_SEND, 10)
        return hourToSend
    }

    fun setUserStatus(status: Boolean) {
        val preferences = context.getSharedPreferences(PREFS_FILE, 0)
        val editor = preferences.edit()
        editor.putBoolean(USER_STATUS, status)
        editor.apply()
    }

    fun getUserStatus(): Boolean {
        val preferences = context.getSharedPreferences(PREFS_FILE, 0)
        val status = preferences.getBoolean(USER_STATUS, false)
        return status
    }

    fun setMaxItemsToShow(itemsToShow: Int) {
        val preferences = context.getSharedPreferences(PREFS_FILE, 0)
        val editor = preferences.edit()
        editor.putInt(MAX_ITEM_TO_SHOW, itemsToShow)
        editor.apply()
    }

    fun getMaxItemsToShow(): Int {
        val preferences = context.getSharedPreferences(PREFS_FILE, 0)
        return preferences.getInt(MAX_ITEM_TO_SHOW, 100)
    }

    data class  UserStatus(val enabled: Boolean, val googlePreviouslyLoggedIn: Boolean)
    data class UserDetails(val hourToSend: Int, val minuteToSend: Int, val maxItemToSend: Int)
    companion object: SingletonHolder<HNLAccountManager, Context>(::HNLAccountManager)
}