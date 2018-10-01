package hackernewslater.williamha.com.hackernewslater.notifications

import android.content.Context
import android.net.Uri
import com.onesignal.OSNotificationOpenResult
import com.onesignal.OneSignal

/**
 * Created by williamha on 8/20/18.
 */
class OneSignalNotificationOpenedHandler constructor(context: Context): OneSignal.NotificationOpenedHandler {

    interface OneSignalNotificationOpenHandlerListener {
        fun onUserClickedNotificationWithUri(uri: Uri)
    }

    private var mListener: OneSignalNotificationOpenHandlerListener? = null

    init {
        if (context is OneSignalNotificationOpenHandlerListener) {
            mListener = context
        }
    }

    override fun notificationOpened(result: OSNotificationOpenResult?) {

        result?.let {
            val data = it.notification.payload.additionalData

            try {
                val url = data.get("url")
                url?.let {
                    val uri = Uri.parse(it.toString())
                    mListener?.let {
                        it.onUserClickedNotificationWithUri(uri)
                    }

                }
            } catch (ex: Exception) {

            }
        }
    }
}