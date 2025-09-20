package citu.edu.stathis.mobile.features.auth.domain.usecase

import com.auth0.android.jwt.JWT
import javax.inject.Inject
import java.util.Date

class TokenValidationUseCase @Inject constructor() {
    fun isTokenExpired(token: String?): Boolean {
        if (token.isNullOrBlank()) return true
        return try {
            val jwt = JWT(token)
            jwt.expiresAt?.before(Date()) ?: true
        } catch (e: Exception) {
            true
        }
    }
} 