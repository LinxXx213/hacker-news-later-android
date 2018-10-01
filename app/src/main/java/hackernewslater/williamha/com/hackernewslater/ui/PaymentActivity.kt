package hackernewslater.williamha.com.hackernewslater.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton
import com.stripe.android.Stripe
import com.stripe.android.TokenCallback
import com.stripe.android.model.Token
import com.stripe.android.view.CardInputListener
import com.stripe.android.view.CardInputWidget
import hackernewslater.williamha.com.hackernewslater.*
import hackernewslater.williamha.com.hackernewslater.exceptions.HNLException
import hackernewslater.williamha.com.hackernewslater.model.StripeToken
import hackernewslater.williamha.com.hackernewslater.model.UserAccountResponse
import hackernewslater.williamha.com.hackernewslater.repository.HackerNewsApiRepo
import hackernewslater.williamha.com.hackernewslater.service.HackerNewsApiService
import java.lang.Exception

/**
 * Created by williamha on 8/6/18.
 */
class PaymentActivity: AppCompatActivity() {

    val context: Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.stripe_credit_card_widget)

        val url = intent.getStringExtra(URL_CONSTANT)
        val apiRepo = HackerNewsApiRepo(this, HackerNewsApiService.instance)
        var chargeButton = findViewById<CircularProgressButton>(R.id.chargeButton)

        val cardWidget = findViewById<CardInputWidget>(R.id.card_input_widget)

        cardWidget.setCardInputListener(object: CardInputListener {
            override fun onPostalCodeComplete() {}
            override fun onExpirationComplete() {}
            override fun onCvcComplete() {}
            override fun onFocusChange(focusField: String?) {}
            override fun onCardComplete() { chargeButton.isEnabled = true }
        })

        chargeButton.setOnClickListener {
            chargeButton.startAnimation()

            val card = cardWidget.card

            card?.let { card ->
                // Card is valid:
                var stripeKey = returnStripeKey()
                val stripe = Stripe(this, stripeKey)
                stripe.createToken(card, object: TokenCallback {
                    override fun onSuccess(token: Token?) {

                        // Send token to server
                        token?.let {
                            apiRepo.chargeUser(StripeToken(it.id)) { user: UserAccountResponse?, error: HNLException? ->
                                error?.let { it ->
                                    showStripeError(it.localizedMessage)
                                    chargeButton.revertAnimation()
                                }

                                user?.let {
                                    val alertDialog = AlertDialog.Builder(context)
                                    alertDialog.setMessage("Success! You can now start reading your selected item immediately.")
                                    alertDialog.setPositiveButton("Ok", { dialog, _ ->
                                        dialog.dismiss()

                                        val value = user.numberOfImmediateReads

                                        val returnIntent = Intent()
                                        returnIntent.putExtra(URL_CONSTANT, url)
                                        returnIntent.putExtra(NUMBER_OF_READS, value)
                                        setResult(Activity.RESULT_OK, returnIntent)
                                        finish()
                                    })
                                    alertDialog.show()
                                }

                                chargeButton.revertAnimation()
                            }
                        }
                    }

                    override fun onError(error: Exception?) {
                        error?.let {
                            showStripeError(it.localizedMessage)
                            chargeButton.revertAnimation()
                        }
                    }
                })
            }
        }
    }

    private fun showStripeError(message: String) {
        val alert = AlertDialog.Builder(this)
        alert.setMessage(message)
        alert.setPositiveButton("Ok", { dialog, which ->
            setResult(Activity.RESULT_CANCELED)
            finish()
        })
        alert.show()
    }

    private fun returnStripeKey(): String {
        if (BuildConfig.DEBUG) {
            return TEST_STRIPE_KEY
        } else {
            return LIVE_STRIPE_KEY
        }
    }
}