package com.radixdlt.radixsamples

import com.radixdlt.client.application.RadixApplicationAPI
import com.radixdlt.client.application.identity.RadixIdentity

/**
 * We use a simple object acting as a Singleton in Kotlin to keep a reference to
 * our identity during the lifecycle of the application.
 * */
object Identity {
    lateinit var myIdentity: RadixIdentity

    val api: RadixApplicationAPI
        get() = RadixApplicationAPI.create(myIdentity)
}
