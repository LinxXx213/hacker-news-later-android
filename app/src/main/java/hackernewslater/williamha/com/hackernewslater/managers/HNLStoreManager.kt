package hackernewslater.williamha.com.hackernewslater.managers

import android.content.Context
import hackernewslater.williamha.com.hackernewslater.SingletonHolder

/**
 * Created by williamha on 8/2/18.
 */
class HNLStoreManager private constructor(val context: Context) {

    val PREFS_FILE = "com.williamha.hackernewslater.prefs"
    val FIRST_LAUNCH = "first_launch"

    fun setIsFirstLaunchOccurred() {
        val preferences = context.getSharedPreferences(PREFS_FILE, 0)
        val editor = preferences.edit()
        editor.putBoolean(FIRST_LAUNCH, false)
        editor.apply()
    }

    fun getIsFirstLaunch(): Boolean {
        val preferences = context.getSharedPreferences(PREFS_FILE, 0)
        return preferences.getBoolean(FIRST_LAUNCH, true)
    }

    companion object: SingletonHolder<HNLStoreManager, Context>(::HNLStoreManager)
}