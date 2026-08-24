package com.kodeelite.nooreislam.core.network

/** Either the payload or a reason. Nothing else leaves the network layer. */
sealed interface ApiResult<out T> {
    data class Ok<T>(val data: T) : ApiResult<T>
    data class Fail(val reason: ApiReason, val code: Int = 0) : ApiResult<Nothing>
}

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Ok -> ApiResult.Ok(transform(data))
    is ApiResult.Fail -> this
}

fun <T> ApiResult<T>.dataOrNull(): T? = (this as? ApiResult.Ok)?.data
