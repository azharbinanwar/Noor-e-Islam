package com.kodeelite.nooreislam.core.platform

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.kodeelite.nooreislam.core.network.ApiClient

/** Images ride the app's own HTTP client, so they carry the same headers and pass the same gates. */
fun installImageLoader(client: ApiClient) {
    SingletonImageLoader.setSafe { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory(client.http)) }
            .build()
    }
}
