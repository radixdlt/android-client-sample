package com.radixdlt.radixsamples

import android.app.Application
import com.radixdlt.client.core.Bootstrap
import com.radixdlt.client.core.RadixUniverse

class RadixSampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // In order to connect to the Radix Universe, we must bootstrap
        // to a specific universe. ALPHANET is currently the recommended one
        RadixUniverse.bootstrap(Bootstrap.ALPHANET)
    }
}