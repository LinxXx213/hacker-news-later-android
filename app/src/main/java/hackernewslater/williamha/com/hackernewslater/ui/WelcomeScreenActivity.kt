package hackernewslater.williamha.com.hackernewslater.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import hackernewslater.williamha.com.hackernewslater.R
import hackernewslater.williamha.com.hackernewslater.TO_REGISTER
import kotlinx.android.synthetic.main.login_signup_activity.*

/**
 * Created by williamha on 8/31/18.
 */
class WelcomeScreenActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.login_signup_activity)

        dismissButton.setOnClickListener {
            val returnIntent = Intent()
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

        registerButton.setOnClickListener {
            val returnIntent = Intent()
            returnIntent.putExtra(TO_REGISTER, true)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }
}