package citu.edu.stathis.mobile.features.auth.domain.repository

import citu.edu.stathis.mobile.features.auth.domain.model.AuthResult
import citu.edu.stathis.mobile.features.auth.domain.model.UserData

import kotlinx.coroutines.flow.Flow

interface IAuthRepository {
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signUp(firstName: String, lastName: String, email: String, password: String): AuthResult
    suspend fun signInWithGoogle(): AuthResult
    suspend fun signInWithMicrosoft(): AuthResult
    suspend fun resetPassword(email: String): AuthResult
    suspend fun signOut(): AuthResult
    fun isLoggedIn(): Flow<Boolean>
    suspend fun getCurrentUser(): UserData?
    suspend fun resendVerificationEmail(email: String): AuthResult
    suspend fun verifyRoleOfCurrentUser(): Boolean
}