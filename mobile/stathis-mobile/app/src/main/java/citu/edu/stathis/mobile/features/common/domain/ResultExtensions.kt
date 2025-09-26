package citu.edu.stathis.mobile.features.common.domain

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Maps a Flow<T> into Flow<Result<T>> with standardized error handling.
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> =
    this
        .map { value: T -> Result.Success(value) as Result<T> }
        .catch { throwable -> emit(throwable.toResultError()) }

/**
 * Wraps a suspending call and maps exceptions to Result.Error.
 */
suspend inline fun <T> safeCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    crossinline block: suspend () -> T
): Result<T> = withContext(dispatcher) {
    try {
        Result.Success(block())
    } catch (t: Throwable) {
        t.toResultError()
    }
}

/**
 * Converts a Retrofit Response<T> into Result<T> with best-effort error parsing.
 */
fun <T> Response<T>.toResult(): Result<T> {
    return if (isSuccessful) {
        val body = body()
        if (body != null) {
            Result.Success(body)
        } else {
            Result.Error("Empty response body", null, code().toString())
        }
    } else {
        Result.Error(parseErrorBody(errorBody()), null, code().toString())
    }
}

fun Throwable.toResultError(): Result.Error {
    return when (this) {
        is HttpException -> {
            val codeString = code().toString()
            Result.Error(message = message(), cause = this, code = codeString)
        }
        is SocketTimeoutException -> Result.Error("Request timed out. Please try again.", this, null)
        is IOException -> Result.Error("Network error. Check your connection and try again.", this, null)
        else -> Result.Error(message = localizedMessage ?: "Unknown error", cause = this, code = null)
    }
}

private fun parseErrorBody(errorBody: ResponseBody?): String {
    return try {
        errorBody?.string()?.takeIf { it.isNotBlank() } ?: "Request failed"
    } catch (_: Exception) {
        "Request failed"
    }
}

/**
 * Maps ClientResponse<T> to Result<T>.
 */
fun <T> ClientResponse<T>.toResult(): Result<T> {
    return if (success && data != null) {
        Result.Success(data)
    } else {
        Result.Error(message = message, cause = null, code = null)
    }
}


