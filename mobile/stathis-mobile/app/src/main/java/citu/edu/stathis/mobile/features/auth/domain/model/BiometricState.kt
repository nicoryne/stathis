package citu.edu.stathis.mobile.features.auth.domain.model

sealed class BiometricState {
    data object NotChecked : BiometricState()
    data object Available : BiometricState()
    data object TokenExpired : BiometricState()
}