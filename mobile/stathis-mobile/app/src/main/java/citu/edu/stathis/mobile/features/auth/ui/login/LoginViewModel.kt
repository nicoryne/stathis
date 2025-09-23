package citu.edu.stathis.mobile.features.auth.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.core.data.AuthTokenManager
import citu.edu.stathis.mobile.features.auth.data.models.BiometricState
import citu.edu.stathis.mobile.features.auth.data.models.LoginResponse
import citu.edu.stathis.mobile.features.auth.domain.usecase.BiometricAuthResultUseCase
import citu.edu.stathis.mobile.features.auth.domain.usecase.LoginResultUseCase
import citu.edu.stathis.mobile.features.auth.data.enums.UserRoles
import citu.edu.stathis.mobile.features.common.domain.Result
import cit.edu.stathis.mobile.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginResultUseCase: LoginResultUseCase,
    private val biometricAuthResultUseCase: BiometricAuthResultUseCase,
    private val authTokenManager: AuthTokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    private val _biometricState = MutableStateFlow<BiometricState>(BiometricState.NotChecked)
    val biometricState: StateFlow<BiometricState> = _biometricState.asStateFlow()

    init {
        refreshBiometricState()
    }

    private fun checkBiometricAvailability() {
        viewModelScope.launch {
            val canPrompt = biometricAuthResultUseCase.canPromptBiometrics()
            _biometricState.value = if (canPrompt) BiometricState.Available else BiometricState.NotAvailable
        }
    }

    fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email) }
            }
            is LoginUiEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.password) }
            }
            is LoginUiEvent.TogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            is LoginUiEvent.Login -> {
                login()
            }
            is LoginUiEvent.BiometricLogin -> {
                loginWithBiometrics()
            }
            is LoginUiEvent.NavigateToRegister -> {
                viewModelScope.launch {
                    _events.emit(LoginEvent.NavigateToRegister)
                }
            }
            is LoginUiEvent.BypassLogin -> {
                bypassLogin()
            }
        }
    }

    private fun login() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result: Result<LoginResponse> = loginResultUseCase(
                email = state.value.email,
                password = state.value.password
            )
            _state.update { it.copy(isLoading = false) }
            when (result) {
                is Result.Success -> _events.emit(LoginEvent.NavigateToHome)
                is Result.Error -> _events.emit(LoginEvent.ShowError(result.message))
            }
        }
    }

    private fun loginWithBiometrics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result: Result<Unit> = biometricAuthResultUseCase.authenticate()
            _state.update { it.copy(isLoading = false) }
            when (result) {
                is Result.Success -> {
                    _events.emit(LoginEvent.NavigateToHome)
                    _biometricState.value = BiometricState.NotChecked
                }
                is Result.Error -> {
                    _events.emit(LoginEvent.ShowError(result.message))
                    checkBiometricAvailability()
                }
            }
        }
    }

    private fun bypassLogin() {
        viewModelScope.launch {
            if (!(BuildConfig.BYPASS_AUTH && BuildConfig.APP_ENV == "local")) {
                _events.emit(LoginEvent.ShowError("Bypass login is disabled"))
                return@launch
            }
            // Save a local debug session so auth gates see a logged-in state
            authTokenManager.saveSessionTokensAndRole(
                accessToken = "debug_access",
                refreshToken = "debug_refresh",
                role = UserRoles.STUDENT
            )
            // Set a test identity for downstream features that rely on physicalId/role
            authTokenManager.updateUserIdentity(physicalId = "debug_user", role = UserRoles.STUDENT)
            _events.emit(LoginEvent.NavigateToHome)
        }
    }

    fun refreshBiometricState() {
        checkBiometricAvailability()
    }
}

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false
)

sealed class LoginUiEvent {
    data class EmailChanged(val email: String) : LoginUiEvent()
    data class PasswordChanged(val password: String) : LoginUiEvent()
    data object TogglePasswordVisibility : LoginUiEvent()
    data object Login : LoginUiEvent()
    data object BiometricLogin : LoginUiEvent()
    data object BypassLogin : LoginUiEvent()
    data object NavigateToRegister : LoginUiEvent()
    // Removed: data object NavigateToForgotPassword : LoginUiEvent()
}

sealed class LoginEvent {
    data object NavigateToHome : LoginEvent()
    data object NavigateToRegister : LoginEvent()
    // Removed: data object NavigateToForgotPassword : LoginEvent()
    data class ShowError(val message: String) : LoginEvent()
}