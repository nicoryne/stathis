package citu.edu.stathis.mobile.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.core.auth.BiometricHelper
import citu.edu.stathis.mobile.features.auth.domain.repository.IAuthRepository
import citu.edu.stathis.mobile.features.auth.ui.register.RegisterState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CoreNavigationViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val biometricHelper: BiometricHelper
) : ViewModel() {

    private val _biometricHelper = MutableStateFlow(biometricHelper)
    val biometricHelperState: StateFlow<BiometricHelper> = _biometricHelper.asStateFlow()

    val isLoggedIn = authRepository.isLoggedIn()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
}