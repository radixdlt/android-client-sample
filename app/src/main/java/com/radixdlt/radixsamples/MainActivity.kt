package com.radixdlt.radixsamples

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import com.radixdlt.client.application.identity.EncryptedRadixIdentity
import com.radixdlt.client.core.RadixUniverse
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var radixAddressTextView: TextView
    private lateinit var identityButton: Button
    private lateinit var deleteIdentityButton: Button
    private lateinit var dataButton: Button
    private lateinit var tokenButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        radixAddressTextView = findViewById(R.id.radixAddress)
        identityButton = findViewById(R.id.createIdentity)
        deleteIdentityButton = findViewById(R.id.deleteIdentity)
        dataButton = findViewById(R.id.storingRetrievingData)
        tokenButton = findViewById(R.id.sendingRetrievingTokens)

        // Choose a location to store the key file and name it
        val myKeyFile = File(filesDir, "keystore.key")

        // Check that the file exists and load identity with default password
        // If no file disable appropriate buttons
        if (myKeyFile.exists()) {
            // NOTE: User would be expected to type the password
            Identity.myIdentity = EncryptedRadixIdentity("123456", myKeyFile.path)
            // Display the Radix Address in Base58 format using RadixUniverse instance
            radixAddressTextView.text = RadixUniverse.getInstance()
                    .getAddressFrom(Identity.myIdentity.getPublicKey())
                    .toString()
            identityButton.isEnabled = false
        } else {
            // Disable/Enable appropriate buttons
            disableEnableButtons(false)
        }

        // Identity button allows for the creation of an EncryptedIdentity with a
        // default password
        identityButton.setOnClickListener {
            // NOTE: Users would be expected to choose their own passwod to encrypt the file
            Identity.myIdentity = EncryptedRadixIdentity("123456", myKeyFile.path)
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
            radixAddressTextView.text = "Address will be shown here once created"
        }

        dataButton.setOnClickListener {
            startActivity(Intent(this, DataActivity::class.java))
        }

        tokenButton.setOnClickListener {
            startActivity(Intent(this, TokenActivity::class.java))
        }
    }

    private fun disableEnableButtons(boolean: Boolean) {
        identityButton.isEnabled = !boolean
        deleteIdentityButton.isEnabled = boolean
        dataButton.isEnabled = boolean
        tokenButton.isEnabled = boolean
    }
}
