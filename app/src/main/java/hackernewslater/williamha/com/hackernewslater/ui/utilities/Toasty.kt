package hackernewslater.williamha.com.hackernewslater.ui.utilities

import android.content.Context
import android.widget.Toast

/**
 * Created by williamha on 7/29/18.
 */
class Toasty constructor(context: Context, message: String, duration:Int = Toast.LENGTH_SHORT) {
    val toast = Toast.makeText(context, message, duration)
}
