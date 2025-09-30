package citu.edu.stathis.mobile.features.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.auth.data.models.LoginResponse
import citu.edu.stathis.mobile.features.auth.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    data class UiState(
        val isSubmitting: Boolean = false,
        val errorMessage: String? = null,
        val success: Boolean = false
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun login(email: String, password: String) {
        if (_state.value.isSubmitting) return
        _state.value = UiState(isSubmitting = true)
        viewModelScope.launch {
            val resp: ClientResponse<LoginResponse> = authRepository.login(email, password)
            _state.value = if (resp.success) UiState(success = true) else UiState(errorMessage = resp.message)
        }
    }
}


