package com.radixdlt.radixsamples

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.radixdlt.client.application.RadixApplicationAPI
import com.radixdlt.client.application.identity.EncryptedRadixIdentity
import com.radixdlt.client.assets.Asset
import com.radixdlt.client.core.RadixUniverse
import com.radixdlt.client.core.address.RadixAddress
import com.radixdlt.client.dapps.messaging.RadixMessaging
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * MainActivity which ensures the creation of a RadixIdentity before allowing
 * any interaction with the api
 * */
class MainActivity : AppCompatActivity() {

    private lateinit var radixAddressTextView: TextView
    private lateinit var identityButton: Button
    private lateinit var deleteIdentityButton: Button
    private lateinit var dataButton: Button
    private lateinit var tokenButton: Button
    private lateinit var radixBalanceTextView: TextView
    private lateinit var getTestTokensButton: Button

    private lateinit var messaging: RadixMessaging
    private lateinit var api: RadixApplicationAPI

    private lateinit var disposableBalance: Disposable

    val FAUCET_ADDRESS = "9egHejbV2z1p1Luy2mER4BXsaHbyM67LdaLrUoJ9YSFRGCw1XPC"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        radixAddressTextView = findViewById(R.id.radixAddress)
        identityButton = findViewById(R.id.createIdentity)
        deleteIdentityButton = findViewById(R.id.deleteIdentity)
        dataButton = findViewById(R.id.storingRetrievingData)
        tokenButton = findViewById(R.id.sendingRetrievingTokens)
        radixBalanceTextView = findViewById(R.id.radixBalanceTextView)
        getTestTokensButton = findViewById(R.id.getTestTokensButton)

        // Choose a location to store the key file and name it
        val myKeyFile = File(filesDir, "keystore.key")

        // Check that the file exists and load identity with default password
        // If no file disable appropriate buttons
        if (myKeyFile.exists()) {
            // NOTE: User would be expected to type the password
            Identity.myIdentity = EncryptedRadixIdentity("123456", myKeyFile.path)

            initMessagingApi()

            // Display the Radix Address in Base58 format using RadixUniverse instance
            radixAddressTextView.text = RadixUniverse.getInstance()
                    .getAddressFrom(Identity.myIdentity.getPublicKey())
                    .toString()
            identityButton.isEnabled = false

            getBalance()
        } else {
            // Disable/Enable appropriate buttons
            disableEnableButtons(false)
        }

        getTestTokensButton .setOnClickListener {
            Toast.makeText(this, "Requesting tokens...", Toast.LENGTH_SHORT).show()
            // Send a message!
            messaging
//            RadixMessaging.instance
                    .sendMessage("Give me some radix!!", RadixAddress.fromString(FAUCET_ADDRESS))
                    .toCompletable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        Toast.makeText(this, "Request sent...", Toast.LENGTH_SHORT)
                                .show()
                    }
        }

        // Identity button allows for the creation of an EncryptedIdentity with a
        // default password
        identityButton.setOnClickListener {
            // NOTE: Users would be expected to choose their own passwod to encrypt the file
            Identity.myIdentity = EncryptedRadixIdentity("123456", myKeyFile.path)

            initMessagingApi()

            radixAddressTextView.text = RadixUniverse.getInstance()
                    .getAddressFrom(Identity.myIdentity.getPublicKey())
                    .toString()

            // Disable/Enable appropriate buttons
            disableEnableButtons(true)
        }

        // Deletes identity file from
        deleteIdentityButton.setOnClickListener {
            // DANGER: If user has not backed up their identity and deletes this file, it will
            //         be unrecoverable.
            File(filesDir, "keystore.key").delete()


            // Disable/Enable appropriate buttons
            disableEnableButtons(false)
            radixBalanceTextView.text = "Balance from address"
            radixAddressTextView.text = "Address will be shown here once created"
        }

        dataButton.setOnClickListener {
            startActivity(Intent(this, DataActivity::class.java))
        }

        tokenButton.setOnClickListener {
            startActivity(Intent(this, TokenActivity::class.java))
        }
    }

    private fun initMessagingApi() {
        // Initialise messaging api
        api = Identity.api
        messaging = RadixMessaging(api)
    }

    // Function which allows for the address balance to be retrieved
    private fun getBalance() {
        radixBalanceTextView.text = "Getting balance..."

        // Calling getMyBalance passing a specific asset returns an observable which can
        // be subscribed to in order to get the latest balance.
        val balance = api.getMyBalance(Asset.TEST)
        disposableBalance = balance.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({
                    radixBalanceTextView.text = BigDecimal((it.amountInSubunits.toDouble() / Asset.TEST.subUnits))
                            .setScale(5, RoundingMode.HALF_UP)
                            .toPlainString()
                }, {
                    Log.e("MainActivity", "Error retrieving balance!", it)
                    Toast.makeText(this, "Error retrieving balance!", Toast.LENGTH_SHORT).show()
                })
    }

    private fun disableEnableButtons(boolean: Boolean) {
        identityButton.isEnabled = !boolean
        deleteIdentityButton.isEnabled = boolean
        dataButton.isEnabled = boolean
        tokenButton.isEnabled = boolean
        getTestTokensButton.isEnabled = boolean
    }

    override fun onDestroy() {
        if (this::disposableBalance.isInitialized) disposableBalance.dispose()
        super.onDestroy()
    }
}
