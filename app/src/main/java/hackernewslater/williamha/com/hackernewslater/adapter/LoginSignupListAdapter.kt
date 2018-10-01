package hackernewslater.williamha.com.hackernewslater.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import hackernewslater.williamha.com.hackernewslater.R
import hackernewslater.williamha.com.hackernewslater.managers.HNLAccountManager
import android.view.inputmethod.EditorInfo
import android.view.KeyEvent
import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.SignInButton
import com.onesignal.OneSignal
import hackernewslater.williamha.com.hackernewslater.PRIVACY_POLICY_URL
import hackernewslater.williamha.com.hackernewslater.TERMS_CONDITIONS_URL
import hackernewslater.williamha.com.hackernewslater.exceptions.HNLException
import hackernewslater.williamha.com.hackernewslater.model.EmailPassword
import hackernewslater.williamha.com.hackernewslater.ui.utilities.Toasty
import java.util.regex.Pattern

/**
 * Created by williamha on 7/17/18.
 */
class LoginSignupListAdapter(context: Context, listener: LoginSignupListAdapterListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val accountManager = HNLAccountManager.getInstance(context)
    private var loginSignupListAdapterListener: LoginSignupListAdapterListener? = listener

    private lateinit var recyclerView: RecyclerView

    var email: String? = null
    var password: String? = null

    interface LoginSignupListAdapterListener {
        fun onLoginSuccess()
        fun onLoginFailure(exception: HNLException?)
        fun onForgotPasswordButtonPressed()
        fun onRegisterSuccess()
        fun onRegisterFailure()
        fun onUserViewWebPage(url: String)
        fun onUserInitiatedGoogleSignIn()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            return LoginViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.login_view_holder, parent, false), parent.context)
        }
        return RegisterViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.register_view_holder, parent, false), parent.context)

    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return 2 // There are only two cells here.
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    inner class LoginViewHolder(v: View, context: Context): RecyclerView.ViewHolder(v) {
        private var context = context
        private var emailTextField: EditText
        private var passwordTextField: EditText
        private var loginButton: CircularProgressButton = v.findViewById(R.id.loginButton)

        private var email: String = ""
        private var password: String = ""


        init {
            emailTextField = v.findViewById<EditText>(R.id.loginEmailTextField)
            passwordTextField = v.findViewById<EditText>(R.id.loginPasswordTextField)
            passwordTextField.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_NULL &&
                        event.action == KeyEvent.ACTION_DOWN) {
                    login()
                }
                true
            }

            val textWatcher = object: TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    s?.let {
                        if (emailTextField.text.hashCode() == it.hashCode()) {
                            email = it.toString()
                        }

                        if (passwordTextField.text.hashCode() == it.hashCode()) {
                            password = it.toString()
                        }
                    }
                    validateButton(email, password, loginButton)
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }

            emailTextField.addTextChangedListener(textWatcher)
            passwordTextField.addTextChangedListener(textWatcher)


            val loginButton = v.findViewById<Button>(R.id.loginButton)
            loginButton.setOnClickListener {
                login()
            }

            val signupButton = v.findViewById<Button>(R.id.signupInsteadButton)
            signupButton.setOnClickListener {
                recyclerView.smoothScrollToPosition(1)
            }

            val forgotPasswordButton = v.findViewById<Button>(R.id.forgotPasswordButton)
            forgotPasswordButton.setOnClickListener {
                loginSignupListAdapterListener?.onForgotPasswordButtonPressed()
            }

            val signInWithGoogleButton = v.findViewById<SignInButton>(R.id.signinGoogleButton)
            signInWithGoogleButton.setOnClickListener {
                loginSignupListAdapterListener?.onUserInitiatedGoogleSignIn()
            }
        }

        private fun returnEmailPasswordFromFields(): EmailPassword? {
            val email = emailTextField.text.toString()
            val password = passwordTextField.text.toString()

            if (!email.isEmpty() && !password.isEmpty()) {
                return EmailPassword(email, password)
            }
            return null
        }

        private fun login() {
            returnEmailPasswordFromFields()?.let {
                loginButton.startAnimation()
                accountManager.login(it.email, it.password) { success, exception ->
                    if (success) {
                        loginSignupListAdapterListener?.onLoginSuccess()
                    } else {
                        loginSignupListAdapterListener?.onLoginFailure(exception)
                    }
                    loginButton.revertAnimation()
                }
            }
        }
    }

    inner class RegisterViewHolder(v: View, context: Context): RecyclerView.ViewHolder(v) {

        private var context = context
        private var loginInsteadButton: Button = v.findViewById(R.id.registerLoginInsteadButton)
        private var emailEditTextField: EditText = v.findViewById(R.id.registerEmailTextField)
        private var passwordEditTextField: EditText = v.findViewById(R.id.registerPasswordTextField)
        private var registerButton: CircularProgressButton = v.findViewById(R.id.registerButton)
        private var termsAndConditions: Button = v.findViewById(R.id.termsAndConditions)
        private var privacyPolicy: Button = v.findViewById(R.id.privacyPolicy)
        private var googleRegisterButton: SignInButton = v.findViewById(R.id.registerGoogleButton)

        private var email: String = ""
        private var password: String = ""

        init {
            loginInsteadButton.setOnClickListener {
                recyclerView.smoothScrollToPosition(0)
            }

            registerButton.setOnClickListener {
                register()
            }

            termsAndConditions.setOnClickListener {
                loginSignupListAdapterListener?.onUserViewWebPage(TERMS_CONDITIONS_URL)
            }

            privacyPolicy.setOnClickListener {
                loginSignupListAdapterListener?.onUserViewWebPage(PRIVACY_POLICY_URL)
            }

            val watcher = object: TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    s?.let {
                        if (emailEditTextField.text.hashCode() == it.hashCode()) {
                            email = it.toString()
                        }

                        if (passwordEditTextField.text.hashCode() == it.hashCode()) {
                            password = it.toString()
                        }
                        validateButton(email, password, registerButton)
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }
            emailEditTextField.addTextChangedListener(watcher)
            passwordEditTextField.addTextChangedListener(watcher)
            passwordEditTextField.setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_NULL &&
                        event.action == KeyEvent.ACTION_DOWN) {
                    register()
                }
                true
            }

            googleRegisterButton.setOnClickListener {
                loginSignupListAdapterListener?.onUserInitiatedGoogleSignIn()
            }
        }

        private fun register() {
            returnEmailPasswordFromFields()?.let {
                registerButton.startAnimation()
                accountManager.register(it.email, it.password) { success, exception ->
                    if (success) {
                        OneSignal.setEmail(it.email)
                        loginSignupListAdapterListener?.onRegisterSuccess()
                        registerButton.revertAnimation()
                    } else {
                        exception?.message?.let {
                            Toasty(context, it).toast.show()
                        }

                        registerButton.revertAnimation()
                        loginSignupListAdapterListener?.onRegisterFailure()
                    }
                }
            }
        }

        private fun returnEmailPasswordFromFields(): EmailPassword? {
            val email = emailEditTextField.text.toString()
            val password = passwordEditTextField.text.toString()

            if (!email.isEmpty() && !password.isEmpty()) {
                return EmailPassword(email, password)
            }
            return null
        }
    }

    // Helpers

    private fun validateButton(email: String, password: String, button: Button) {
        val isValid: Boolean = (password.length >= 6 && isEmailValid(email))
        button.isEnabled = isValid
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