package com.radixdlt.radixsamples

import com.radixdlt.client.application.RadixApplicationAPI
import com.radixdlt.client.application.identity.EncryptedRadixIdentity

/**
 * We use a simple object acting as a Singleton in Kotlin to keep a reference to
 * our identity. During the lifecycle of the application.
 * */
object Identity {
    lateinit var myIdentity: EncryptedRadixIdentity

    val api: RadixApplicationAPI
        get() = RadixApplicationAPI.create(myIdentity)
}
