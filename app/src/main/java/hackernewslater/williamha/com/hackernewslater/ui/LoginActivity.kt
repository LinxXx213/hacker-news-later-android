package hackernewslater.williamha.com.hackernewslater.ui

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import hackernewslater.williamha.com.hackernewslater.*
import hackernewslater.williamha.com.hackernewslater.managers.HNLAccountManager
import hackernewslater.williamha.com.hackernewslater.ui.fragments.LoginSignUpRecyclerViewFragment
import hackernewslater.williamha.com.hackernewslater.ui.fragments.RegistrationSuccessFragment
import hackernewslater.williamha.com.hackernewslater.ui.utilities.Toasty
import kotlinx.android.synthetic.main.activity_login_sign_up.*


class LoginActivity : AppCompatActivity(),
        LoginSignUpRecyclerViewFragment.LoginSignUpRecyclerViewFragmentListener,
        RegistrationSuccessFragment.RegistrationSuccessFragmentListener {

    val loginSignupRecyclerFragment = LoginSignUpRecyclerViewFragment.newInstance()
    val registrationSuccessFragment = RegistrationSuccessFragment.newInstance()
    var recyclerViewPath = ""
    private var fragmentContainer: FrameLayout? = null

    private var googleSignInClient: GoogleSignInClient? = null
    private val RC_SIGN_IN = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_sign_up)
        fragmentContainer = findViewById(R.id.login_activity_fragment_container)

        setSupportActionBar(loginActivityToolbar)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowTitleEnabled(false)
        }

        supportFragmentManager.beginTransaction().replace(R.id.login_activity_fragment_container, loginSignupRecyclerFragment).commit()

        determinePath()

        setupGoogleClientSignIn()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun determinePath() {
        if (intent.data != null) {
            intent.data.path?.let { path ->
                when(path) {
                    VERIFIED_LOGIN_PATH -> {
                        registrationSuccessFragment.state = VERIFIED_LOGIN_PATH
                        showRegistrationSuccess()
                    }

                    PASSWORD_RESET_PATH -> {
                        Toasty(this, "Password successfully changed").toast.show()
                        recyclerViewPath = LOGIN_PATH
                    }
                }
            }
        } else {
            val selectedPath = intent.getStringExtra(SELECTED_PATH)
            selectedPath?.let {
                when (it) {
                    LOGIN_PATH -> {
                        recyclerViewPath = LOGIN_PATH
                    }

                    REGISTER_PATH -> {
                        recyclerViewPath = REGISTER_PATH
                    }
                }
            }
        }
    }

    private fun showRegistrationSuccess() {
        // TODO: Look into bug regarding performing this after onSaveInstanceState
        supportFragmentManager.beginTransaction().replace(R.id.login_activity_fragment_container, registrationSuccessFragment).commit()
    }

    // LoginSignUpRecyclerViewFragmentListener
    override fun onRegistrationSuccess() {
        showRegistrationSuccess()
    }

    override fun onUserInitiatedGoogleLoginRegistration() {
        googleSignIn()
    }

    // RegistrationSuccessFragmentListener
    override fun userDismissedSuccessView() {
        if (isTaskRoot) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            finish()
        }
    }

    // Google Sign In

    private fun setupGoogleClientSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun googleSignIn() {
        googleSignInClient?.let{
            val googleSignInIntent = it.signInIntent
            startActivityForResult(googleSignInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {

            val progressBar = findViewById<ProgressBar>(R.id.loginProgressBar)
            progressBar.visibility = View.VISIBLE

            try {
                val googleSignInResult = GoogleSignIn.getSignedInAccountFromIntent(data)

                // Send this token to the server
                val token = googleSignInResult.result.idToken

                token?.let {
                    val accountManager = HNLAccountManager.getInstance(this)
                    accountManager.registerWithGoogle(it) { success, userStatus, exception ->
                        progressBar.visibility = View.INVISIBLE
                        if (success) {

                            if (userStatus != null && userStatus.googlePreviouslyLoggedIn) {
                                loginSignupRecyclerFragment.handleSuccessfulLogin()
                                loginSignupRecyclerFragment.broadcastIntent()
                            } else {
                                showRegistrationSuccess()
                            }

                        } else {
                            exception?.message?.let {
                                Toasty(this, it).toast.show()
                            }
                        }
                    }
                }

            } catch (ex: Exception) {
                progressBar.visibility = View.INVISIBLE
                Toasty(this, "Unable to sign in with Google").toast.show()
            }
        }
    }
}
