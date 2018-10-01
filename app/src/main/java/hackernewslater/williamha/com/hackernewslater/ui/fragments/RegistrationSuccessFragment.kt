package hackernewslater.williamha.com.hackernewslater.ui.fragments

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import hackernewslater.williamha.com.hackernewslater.PASSWORD_RESET_PATH
import hackernewslater.williamha.com.hackernewslater.R
import hackernewslater.williamha.com.hackernewslater.VERIFIED_LOGIN_PATH
import hackernewslater.williamha.com.hackernewslater.exceptions.HNLException
import hackernewslater.williamha.com.hackernewslater.managers.HNLAccountManager
import hackernewslater.williamha.com.hackernewslater.ui.MainActivity
import hackernewslater.williamha.com.hackernewslater.ui.utilities.Toasty
import kotlinx.android.synthetic.main.fragment_registration_success.*

/**
 * Created by williamha on 8/2/18.
 */
class RegistrationSuccessFragment: Fragment() {

    var listener: RegistrationSuccessFragmentListener? = null
    var state = ""

    interface RegistrationSuccessFragmentListener {
        fun userDismissedSuccessView()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is RegistrationSuccessFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(context!!.toString() + "must implement the RegistrationSuccessFragmentListener!")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_registration_success, container, false)

        val successButton = view.findViewById<Button>(R.id.registrationSuccessConfirmButton)
        val header = view.findViewById<TextView>(R.id.signedUpHeader)
        val subHeader = view.findViewById<TextView>(R.id.emailOnWaySubtitle)

        when(state) {
            VERIFIED_LOGIN_PATH -> {
                header.text = "Account Verified!"
                subHeader.text = "You can now start queuing your items!"
                successButton.setOnClickListener {
                    val accountManager = HNLAccountManager.getInstance(activity as Context)
                    accountManager.handleSuccessfulLoginRegistration(false) { loggedInMessage, exception ->

                        if (exception == null) {
                            val alertDialog = AlertDialog.Builder(activity as Context)
                            alertDialog.setMessage(loggedInMessage)
                            alertDialog.setPositiveButton("Got it!", {_, _ ->
                                listener?.userDismissedSuccessView()
                            })
                            alertDialog.show()
                        } else {
                            HNLException.handleHNLException(activity as Context, exception) {}
                        }
                    }
                }
            }

            else -> {
                successButton.setOnClickListener {
                    listener?.userDismissedSuccessView()
                }
            }
        }

        val imageView = view.findViewById<ImageView>(R.id.checkAnimationImageView)
        val animatable = imageView.drawable as Animatable
        animatable.start()
        return view
    }

    companion object {
        fun newInstance(): RegistrationSuccessFragment {
            return RegistrationSuccessFragment()
        }
    }

}