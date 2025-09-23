package citu.edu.stathis.mobile.core.util

import retrofit2.Response
import timber.log.Timber

/**
 * Utility class for handling network responses in a standardized way
 */
object NetworkResult {
    const val NETWORK_ERROR = "Network error occurred"
    const val UNKNOWN_ERROR = "An unknown error occurred"
    const val EMPTY_RESPONSE = "Empty response received"
}

/**
 * Extension function to handle API responses in a standardized way
 * @param onSuccess callback to be invoked when the response is successful
 */
suspend inline fun <T> handleApiResponse(
    response: Response<T>,
    crossinline onSuccess: suspend (T) -> Unit
) {
    try {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                onSuccess(body)
            } else {
                Timber.e("Empty response body received")
                throw Exception(NetworkResult.EMPTY_RESPONSE)
            }
        } else {
            val errorMessage = "HTTP Error: ${response.code()} - ${response.message()}"
            Timber.e(errorMessage)
            throw Exception(errorMessage)
        }
    } catch (e: Exception) {
        Timber.e(e, "API request failed: ${e.message}")
        throw e
    }
}
