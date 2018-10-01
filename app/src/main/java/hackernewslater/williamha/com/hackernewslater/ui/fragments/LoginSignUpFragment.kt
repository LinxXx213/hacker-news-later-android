package hackernewslater.williamha.com.hackernewslater.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import hackernewslater.williamha.com.hackernewslater.R
import hackernewslater.williamha.com.hackernewslater.REGISTER_PATH
import hackernewslater.williamha.com.hackernewslater.SELECTED_PATH
import hackernewslater.williamha.com.hackernewslater.ui.LoginActivity

/**
 * Created by williamha on 7/16/18.
 */
class LoginSignUpFragment: Fragment() {
    private var listener: LoginSignUpFragment.LoginSignUpFragmentListener? = null

    interface LoginSignUpFragmentListener {
        fun onUserDismissed()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.login_signup_activity, container, false)
        val dismissButton = view.findViewById<View>(R.id.dismissButton)
        dismissButton.setOnClickListener {
            listener?.onUserDismissed()
        }

        val registerButton = view.findViewById<View>(R.id.registerButton)
        registerButton.setOnClickListener {
            val intent = Intent(context, LoginActivity::class.java)
            intent.putExtra(SELECTED_PATH, REGISTER_PATH)
            startActivity(intent)
        }

        return view
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is LoginSignUpFragment.LoginSignUpFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement LoginSignUpFragmentListener")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun newInstance(): LoginSignUpFragment {
            return LoginSignUpFragment()
        }
    }
}