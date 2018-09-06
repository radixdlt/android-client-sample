package com.radixdlt.radixsamples

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.radixdlt.client.application.RadixApplicationAPI
import com.radixdlt.client.application.objects.Data
import com.radixdlt.client.application.translate.InsufficientFundsException
import com.radixdlt.client.assets.Amount
import com.radixdlt.client.assets.Asset
import com.radixdlt.client.core.address.RadixAddress
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * TokenActivity demonstrates how it is possible to send tokens with unencrypted or encrypted
 * data attached on the Radix DLT ledger.
 * */
class TokenActivity : AppCompatActivity() {

    private val TAG = TokenActivity::class.java.simpleName

    private lateinit var disposableStatus: Disposable
    private lateinit var disposableTransfers: Disposable
    private var transactions = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_token)

        Log.d(TAG, "TokenActivity onCreate")

        val tokenAddressEditText = findViewById<EditText>(R.id.tokenAddressEditText)
        val tokenAmountEditText = findViewById<EditText>(R.id.tokenAmountEditText)
        val tokenMessageEditText = findViewById<EditText>(R.id.tokenMessageEditText)
        val tokenSendTokenButton = findViewById<Button>(R.id.tokenSendTokenButton)
        val tokenStatusTextView = findViewById<TextView>(R.id.tokenStatusTextView)
        val tokensRetrieveAllTransfersButton = findViewById<Button>(R.id.tokensRetrieveAllTransfersButton)
        val tokenTransferTextView = findViewById<TextView>(R.id.tokenTransferTextView)

        val api = Identity.api

        tokenSendTokenButton.isEnabled = false


        // -------------------------------------------------------------------
        // TODO: below functionality needs some library modifications expected in a future update

        // HACK: For now we need to retrieve all transactions before we can send any tokens
        //       else an insufficientFunds exception will be thrown.
        Handler().postDelayed({
            tokensRetrieveAllTransfersButton.performClick()
        }, 500)

        // -------------------------------------------------------------------


        // Below button executes the action of sending tokens
        tokenSendTokenButton.setOnClickListener {

            val data: Data
            val result: RadixApplicationAPI.Result

            try {
                val amount = if (tokenAmountEditText.text.isNullOrEmpty()) {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                } else {
                    tokenAmountEditText.text.toString().toBigDecimal()
                }

                if (tokenMessageEditText.text.isNotEmpty()) {
                    // Builds encrypted data
                    // see DataActivity for extra details
                    data = Data.DataBuilder()
                            .bytes(tokenMessageEditText.text.toString().toByteArray())
                            .addReader(api.myPublicKey)
                            .build()

                    // To attach unencrypted messages use below builder
                    // see DataActivity for extra details
//                    data = Data.DataBuilder()
//                            .bytes(tokenMessageEditText.text.toString().toByteArray())
//                            .unencrypted()
//                            .build()

                    // Calling the sendTokens() api, the previously build data is passed as an
                    // argument and hence attached to the transaction.
                    result = api.sendTokens(RadixAddress.fromString(
                            tokenAddressEditText.text.toString().trim()),
                            Amount.of(amount, Asset.TEST),
                            data)
                } else {
                    // Calling the sendTokens() api without data only sends the transaction
                    result = api.sendTokens(RadixAddress.fromString(
                            tokenAddressEditText.text.toString().trim()),
                            Amount.of(amount, Asset.TEST))
                }

                // It is possible to subscribe to the result by converting it to an observable
                // leveraging rx streams.
                disposableStatus = result.toObservable()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe ({ status ->
                            tokenStatusTextView.text = status.getState().name
                        }, { _ ->
                            Toast.makeText(this, "You don't have enough tokens!", Toast.LENGTH_SHORT).show()
                        })
            } catch (e: Exception) {
                Log.e(TAG, "Error sending tokens", e)
                when (e) {
                    is InsufficientFundsException -> Log.d(TAG, "You don't have enough tokens! Exception")
                    is IllegalArgumentException -> Toast.makeText(this, "This amount is not allowed for subunit value ${Asset.TEST.subUnits}", Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(this, "Wrong address format, check again!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Below button retrieves all token transfers of a particular token
        tokensRetrieveAllTransfersButton.setOnClickListener {

            if (tokenSendTokenButton.isEnabled ) {
                Toast.makeText(this, "Already listening for new transactions...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            tokenStatusTextView.text = "Retrieving transactions..."

            // Calling getMyTokenTransfers from the api passing an asset name Asset will return
            // all the transfers from that particular token.
            //
            // NOTE: For knowing if transfers where sent or received, the TokenTransfer in the
            //       subscription has various properties such as to, from etc
            val transfers = api.getMyTokenTransfers(Asset.TEST)
            disposableTransfers = transfers.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe ({ tx ->
                        Log.d(TAG, tx.subUnitAmount.toString())
                        transactions = transactions.plus("${(tx.subUnitAmount / Asset.TEST.subUnits.toDouble())}\n")

                        tokenTransferTextView.text = transactions

                        // Prevent sending if no tokens retrieved to prevent insufficient funds exception
                        if (!tokenSendTokenButton.isEnabled) {
                            tokenSendTokenButton.isEnabled = true
                            tokenStatusTextView.text = "STATUS"
                        }
                    }, { _ ->
                        tokenSendTokenButton.isEnabled = false
                        Toast.makeText(this, "Something went wrong, try retrieving again!", Toast.LENGTH_SHORT).show()
                    })
        }
    }

    override fun onDestroy() {
        if (this::disposableStatus.isInitialized) disposableStatus.dispose()
        if (this::disposableTransfers.isInitialized) disposableTransfers.dispose()
        super.onDestroy()
    }
}
