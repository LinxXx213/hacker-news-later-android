package hackernewslater.williamha.com.hackernewslater.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import hackernewslater.williamha.com.hackernewslater.*
import hackernewslater.williamha.com.hackernewslater.adapter.LoginSignupListAdapter
import hackernewslater.williamha.com.hackernewslater.exceptions.HNLException
import hackernewslater.williamha.com.hackernewslater.managers.HNLAccountManager
import hackernewslater.williamha.com.hackernewslater.ui.ForgotPasswordActivity
import hackernewslater.williamha.com.hackernewslater.ui.LoginActivity
import hackernewslater.williamha.com.hackernewslater.ui.MainActivity
import hackernewslater.williamha.com.hackernewslater.ui.utilities.Toasty

/**
 * Created by williamha on 8/2/18.
 */
class LoginSignUpRecyclerViewFragment: Fragment(),
        LoginSignupListAdapter.LoginSignupListAdapterListener {

    interface LoginSignUpRecyclerViewFragmentListener {
        fun onRegistrationSuccess()
        fun onUserInitiatedGoogleLoginRegistration()
    }

    private var listener: LoginSignUpRecyclerViewFragment.LoginSignUpRecyclerViewFragmentListener? = null
    private lateinit var loginRegisterListAdapter: LoginSignupListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_signup_login_reycler_view, container, false)
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        val snapHelper = PagerSnapHelper()

        val recyclerView = view.findViewById<RecyclerView>(R.id.signupLoginRecyclerViewInFragment)

        recyclerView.layoutManager = layoutManager
        snapHelper.attachToRecyclerView(recyclerView)

        loginRegisterListAdapter = LoginSignupListAdapter(activity as Context, this)
        recyclerView.adapter = loginRegisterListAdapter

        val activity = activity as LoginActivity
        activity.let {

            when(it.recyclerViewPath) {
                LOGIN_PATH -> {
                    recyclerView.scrollToPosition(0)
                }

                VERIFIED_LOGIN_PATH -> {
                    recyclerView.scrollToPosition(0)
                    val toast = Toasty(it, "Account verified!").toast
                    toast.setGravity(Gravity.TOP, 0, 0)
                    toast.show()
                }

                REGISTER_PATH -> {
                    recyclerView.scrollToPosition(1)
                }

                else -> {
                    recyclerView.scrollToPosition(1)
                }
            }
        }

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is LoginSignUpRecyclerViewFragment.LoginSignUpRecyclerViewFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement LoginSignUpRecyclerViewFragmentListener")
        }
    }

    // LoginSignupListAdapterListener

    override fun onLoginSuccess() {
        handleSuccessfulLogin()
    }

    override fun onLoginFailure(exception: HNLException?) {
        exception?.let {
            var toast = Toast.makeText(activity, it.message, Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
    }

    override fun onForgotPasswordButtonPressed() {
        val intent = Intent(activity as Context, ForgotPasswordActivity::class.java)
        startActivity(intent)
    }

    override fun onRegisterSuccess() {
        listener?.onRegistrationSuccess()
        broadcastIntent()
    }

    override fun onRegisterFailure() {

    }

    override fun onUserInitiatedGoogleSignIn() {
        listener?.onUserInitiatedGoogleLoginRegistration()
    }

    override fun onUserViewWebPage(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    fun broadcastIntent() {
        val localBroadcastManager = LocalBroadcastManager.getInstance(activity as Context)
        val localIntent = Intent(USER_AUTH_STATE_CHANGED)
        localBroadcastManager.sendBroadcast(localIntent)
    }

    fun handleSuccessfulLogin() {
        val accountManager = HNLAccountManager.getInstance(activity as Context)
        accountManager.handleSuccessfulLoginRegistration(true) { message, exception ->
            val alertDialog = AlertDialog.Builder(activity as Context)
            alertDialog.setMessage(message)
            alertDialog.setPositiveButton("Got it!", {_, _ ->
                activity?.let {
                    if (it.isTaskRoot) {
                        val intent = Intent(it as Context, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        it.finish()
                    }
                }

                // Broadcast its success
                broadcastIntent()
            })
            alertDialog.show()
        }
    }

    companion object {
        fun newInstance(): LoginSignUpRecyclerViewFragment {
            return LoginSignUpRecyclerViewFragment()
        }
    }
}