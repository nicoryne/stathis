package citu.edu.stathis.mobile.core.data.models

data class ClientResponse<T> (
    val success: Boolean,
    val message: String,
    val data: T? = null
)