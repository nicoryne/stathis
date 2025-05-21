package citu.edu.stathis.mobile.features.auth.ui.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.auth.domain.model.AuthResult
import citu.edu.stathis.mobile.features.auth.domain.usecase.ResetPasswordUseCase
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
class ForgotPasswordViewModel @Inject constructor(
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ForgotPasswordEvent>()
    val events: SharedFlow<ForgotPasswordEvent> = _events.asSharedFlow()

    fun onEvent(event: ForgotPasswordUiEvent) {
        when (event) {
            is ForgotPasswordUiEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email) }
            }
            is ForgotPasswordUiEvent.ResetPassword -> {
                resetPassword()
            }
            is ForgotPasswordUiEvent.NavigateToLogin -> {
                viewModelScope.launch {
                    _events.emit(ForgotPasswordEvent.NavigateToLogin)
                }
            }
        }
    }

    private fun resetPassword() {
        val email = state.value.email

        if (email.isBlank()) {
            viewModelScope.launch {
                _events.emit(ForgotPasswordEvent.ShowError("Please enter your email"))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = resetPasswordUseCase(email)

            _state.update { it.copy(isLoading = false) }

            when (result) {
                is AuthResult.Success -> {
                    _events.emit(ForgotPasswordEvent.ResetEmailSent)
                }
                is AuthResult.Error -> {
                    _events.emit(ForgotPasswordEvent.ShowError(result.message))
                }
            }
        }
    }
}

data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false
)

sealed class ForgotPasswordUiEvent {
    data class EmailChanged(val email: String) : ForgotPasswordUiEvent()
    data object ResetPassword : ForgotPasswordUiEvent()
    data object NavigateToLogin : ForgotPasswordUiEvent()
}

sealed class ForgotPasswordEvent {
    data object NavigateToLogin : ForgotPasswordEvent()
    data object ResetEmailSent : ForgotPasswordEvent()
    data class ShowError(val message: String) : ForgotPasswordEvent()
}