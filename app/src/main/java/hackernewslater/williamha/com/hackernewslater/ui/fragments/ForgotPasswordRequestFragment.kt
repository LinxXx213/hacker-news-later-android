package hackernewslater.williamha.com.hackernewslater.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton
import hackernewslater.williamha.com.hackernewslater.R
import hackernewslater.williamha.com.hackernewslater.managers.HNLAccountManager
import java.lang.RuntimeException
import java.util.regex.Pattern

/**
 * Created by williamha on 7/20/18.
 */
class ForgotPasswordRequestFragment: Fragment() {

    private var mListener: ForgotPasswordRequestFragmentListener? = null

    private var forgotPasswordButton: CircularProgressButton? = null
    private var enteredEmail: String? = null

    interface ForgotPasswordRequestFragmentListener {
        fun testCallback()
    }

    companion object {
        fun newInstance(): ForgotPasswordRequestFragment {
            return ForgotPasswordRequestFragment()
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is ForgotPasswordRequestFragmentListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement ForgotPasswordRequestFragmentListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.forgot_password_request_fragment, container, false)
        forgotPasswordButton = view.findViewById<CircularProgressButton>(R.id.forgotPasswordButton)
        forgotPasswordButton?.setOnClickListener {
            forgotPasswordButton?.startAnimation()
            onSendPasswordEmailButtonPressed(enteredEmail)
        }

        setupTextWatcher(view)

        return view
    }

    private fun setupTextWatcher(view: View) {
        val forgotPasswordEmailTextField = view.findViewById<EditText>(R.id.forgotPasswordEmailTextField)

        forgotPasswordEmailTextField?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()
                validate(email)
                enteredEmail = email
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
    }

    private fun onSendPasswordEmailButtonPressed(email: String?) {
        if (email != null) {
            if (!email.isEmpty() && isEmailValid(email)) {

                val activity = (activity as AppCompatActivity)
                HNLAccountManager.getInstance(activity).forgotPassword(email) {

                    forgotPasswordButton?.revertAnimation()

                    val alertDialog = AlertDialog.Builder(activity)
                    alertDialog.setMessage("If your account is found, a reset link will be sent to the email you provided.")
                            .setPositiveButton("Ok", { _, _ ->
                                activity.finish()
                            })
                    alertDialog.show()
                }
            }
        }
    }

    // Helpers
    private fun validate(email: String) {
        forgotPasswordButton?.isEnabled = isEmailValid(email)
    }

    private fun isEmailValid(email: String): Boolean {
        return Pattern.compile(
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                        + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
                        + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
        ).matcher(email).matches()
    }
}