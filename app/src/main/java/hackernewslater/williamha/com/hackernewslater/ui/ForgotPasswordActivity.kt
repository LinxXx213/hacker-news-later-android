package hackernewslater.williamha.com.hackernewslater.ui

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.FrameLayout
import hackernewslater.williamha.com.hackernewslater.R
import hackernewslater.williamha.com.hackernewslater.ui.fragments.ForgotPasswordRequestFragment
import hackernewslater.williamha.com.hackernewslater.ui.fragments.LoginSignUpFragment
import kotlinx.android.synthetic.main.forgot_password_activity.*

/**
 * Created by williamha on 7/19/18.
 */
class ForgotPasswordActivity: AppCompatActivity(), ForgotPasswordRequestFragment.ForgotPasswordRequestFragmentListener {

    private var forgotPasswordRequestFragment = ForgotPasswordRequestFragment.newInstance()
    private var fragmentContainer: FrameLayout? = null

    private val LOGIN_PATH = "/accounts/login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.forgot_password_activity)
        fragmentContainer = findViewById(R.id.forgot_password_activity_fragment_container)
        supportFragmentManager.beginTransaction().add(R.id.forgot_password_activity_fragment_container, forgotPasswordRequestFragment).commit()

        setSupportActionBar(forgotPasswordActivityToolbar)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowTitleEnabled(false)
        }
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

    override fun testCallback() {

    }


}