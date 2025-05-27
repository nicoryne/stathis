package citu.edu.stathis.mobile.features.auth.data.models

sealed class BiometricState {

    data object NotChecked : BiometricState()

    data object Available : BiometricState()

    data object NotEnrolled : BiometricState()

    data object NotAvailable : BiometricState()

    data object LockedOut : BiometricState()

    data class Error(val message: String?) : BiometricState()

}