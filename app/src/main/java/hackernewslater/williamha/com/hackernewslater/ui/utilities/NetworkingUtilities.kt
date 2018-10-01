package hackernewslater.williamha.com.hackernewslater.ui.utilities

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import hackernewslater.williamha.com.hackernewslater.SingletonHolder

/**
 * Created by williamha on 8/8/18.
 */
class NetworkingUtilities constructor(val context: Context) {

    fun isConnectedToInternet(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
        return isConnected
    }

    companion object: SingletonHolder<NetworkingUtilities, Context>(::NetworkingUtilities)
}