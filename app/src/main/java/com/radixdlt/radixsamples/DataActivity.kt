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
import io.reactivex.schedulers.Schedulers

class DataActivity : AppCompatActivity() {

    val TAG = DataActivity::class.java.simpleName
    var text: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data)

        Log.d(TAG, "DataActivity onCreate")

        val api = Identity.api
        val myRadixAddress = RadixUniverse.getInstance()
                .getAddressFrom(Identity.myIdentity.getPublicKey())

        val storeDataEditText = findViewById<EditText>(R.id.storeDataEditText)
        val storeEncryptedButton = findViewById<Button>(R.id.storeEncryptedButton)
        val storeUnencryptedButton = findViewById<Button>(R.id.storeUnencryptedButton)
        val readMessagesButton = findViewById<Button>(R.id.readMessagesButton)
        val unencryptedDataTextView = findViewById<TextView>(R.id.unencryptedDataTextView)
        val statusTextView = findViewById<TextView>(R.id.statusTextView)

        storeEncryptedButton.setOnClickListener {
            val myPublicKey = api.myPublicKey
            val data = Data.DataBuilder()
                    .bytes(storeDataEditText.text.toString().toByteArray())
                    .addReader(myPublicKey)
                    .build()

            api.storeData(data, myRadixAddress)
                    .toCompletable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Toast.makeText(this, "Data encrypted and stored", Toast.LENGTH_SHORT).show()
                    }, { error ->
                        Log.e(TAG, "Unable to store encrypted data", error)
                    })
        }

        storeUnencryptedButton.setOnClickListener {
            val data = Data.DataBuilder()
                    .bytes(storeDataEditText.text.toString().toByteArray())
                    .unencrypted()
                    .build()

            api.storeData(data, myRadixAddress)
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

        readMessagesButton.setOnClickListener {
            val readable = api.getReadableData(myRadixAddress)
            readable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { unencryptedData ->
                        text = text.plus("${String(unencryptedData.data)}\n")
                        unencryptedDataTextView.text = text
                    }
            }
    }
}
