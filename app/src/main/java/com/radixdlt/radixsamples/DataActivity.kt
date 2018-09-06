package com.radixdlt.radixsamples

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.radixdlt.client.application.objects.Data
import com.radixdlt.client.core.RadixUniverse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * DataActivity demonstrates how it is possible to store both unencrypted or encrypted
 * data on the Radix DLT ledger.
 * */
class DataActivity : AppCompatActivity() {

    private val TAG = DataActivity::class.java.simpleName
    private var text: String? = ""

    private lateinit var disposableStoring: Disposable
    private lateinit var disposableReadable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data)

        Log.d(TAG, "DataActivity onCreate")

        val storeDataEditText = findViewById<EditText>(R.id.storeDataEditText)
        val storeEncryptedButton = findViewById<Button>(R.id.storeEncryptedButton)
        val storeUnencryptedButton = findViewById<Button>(R.id.storeUnencryptedButton)
        val readMessagesButton = findViewById<Button>(R.id.readMessagesButton)
        val unencryptedDataTextView = findViewById<TextView>(R.id.unencryptedDataTextView)
        val statusTextView = findViewById<TextView>(R.id.statusTextView)

        // First a RadixApplicationApi is required which is created
        // with the use of a radix identity

        // The RadixApplicationApi allows for the direct interaction with the Radix DLT ledger
        // as well as contain data such as the public key for the identity that created it.
        val api = Identity.api

        val myRadixAddress = RadixUniverse.getInstance()
                .getAddressFrom(api.myPublicKey)

        // Below button stores encrypted data on the ledger
        storeEncryptedButton.setOnClickListener {

            // The builder below is used to create a Data Object which is encrypted
            // by the use of readers. The addReader method takes a key which is used
            // to encrypt the data with.
            val data = Data.DataBuilder()
                    .bytes(storeDataEditText.text.toString().toByteArray())
                    .addReader(api.myPublicKey)
                    .build()

            // Calling the store data api, the previously build data is passed as an
            // argument and we can call either the toObservable or toCompletable methods
            // in order to leverage the use of rx streams.
            //
            // Note: In Android we are making use of RxAndroid where we can choose to observe the
            // results on the mainThread() if we plan to make any UI changes.
            api.storeData(data)
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ status ->
                        statusTextView.text = status.getState().name
                    },{ error ->
                        Log.e(TAG, "Unable to store encrypted data", error)
                    }, {
                        Toast.makeText(this, "Data encrypted and stored", Toast.LENGTH_SHORT).show()
                    })
        }

        storeUnencryptedButton.setOnClickListener {

            // The builder below is used to create an unencrypted Data Object which is done
            // by using the appropriate method.
            val data = Data.DataBuilder()
                    .bytes(storeDataEditText.text.toString().toByteArray())
                    .unencrypted()
                    .build()

            // Calling the store data api, the previously build data is passed as an
            // argument and we can call either the toObservable or toCompletable methods
            // in order to leverage the use of rx streams and subscribe to the result.
            //
            // Note: In Android we are making use of RxAndroid where we can choose to observe the
            // results on the mainThread() if we plan to make any UI changes.
            disposableStoring = api.storeData(data, myRadixAddress)
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ status ->
                        statusTextView.text = status.getState().name
                    }, { error ->
                        Log.e(TAG, "Unable to store unencrypted data", error)
                    }, {
                        Toast.makeText(this, "Unencrypted data stored",
                                Toast.LENGTH_SHORT).show()
                    })
        }

        // Below button retrieves all messages from an address
        readMessagesButton.setOnClickListener {

            if (unencryptedDataTextView.text.isNotBlank()) {
                Toast.makeText(this, "Already listening for new messages...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // To read all the data stored in a particular address simply call getReadableData
            // and subscribe to the observable.
            // Using RxAndroid to observe on the mainThread
            val readable = api.getReadableData(myRadixAddress)
            disposableReadable = readable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { unencryptedData ->
                        text = text.plus("${String(unencryptedData.data)}\n")
                        unencryptedDataTextView.text = text
                    }
            }
    }

    override fun onDestroy() {
        if (this::disposableStoring.isInitialized) disposableStoring.dispose()
        if (this::disposableReadable.isInitialized) disposableReadable.dispose()
        super.onDestroy()
    }
}
