package citu.edu.stathis.mobile.features.common.domain

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val code: String? = null
    ) : Result<Nothing>()
}


