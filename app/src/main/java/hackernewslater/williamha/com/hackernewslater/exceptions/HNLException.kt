package hackernewslater.williamha.com.hackernewslater.exceptions

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

/**
 * Created by williamha on 7/24/18.
 */
class HNLException constructor(message: String? = "An error occurred."): Exception(message) {

    companion object {
        fun handleHNLException(context: Context, exception: HNLException?, callback:() -> Unit) {
            exception?.let { it ->
                val message = it.message

                message?.let {
                    val alertDialog = android.support.v7.app.AlertDialog.Builder(context)
                    alertDialog.setMessage(message)
                    alertDialog.setPositiveButton("Got it!", { dialog, _ ->
                        dialog.dismiss()
                    })
                    alertDialog.show()
                }
            }
        }
    }
}