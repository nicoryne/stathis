package citu.edu.stathis.mobile.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.core.auth.BiometricHelper
import citu.edu.stathis.mobile.features.auth.data.repository.AuthRepository
import citu.edu.stathis.mobile.features.auth.domain.usecase.AuthState
import citu.edu.stathis.mobile.features.auth.domain.usecase.HandleAuthStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoreNavigationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val handleAuthStateUseCase: HandleAuthStateUseCase,
    biometricHelper: BiometricHelper
) : ViewModel() {

    private val _biometricHelperState = MutableStateFlow(biometricHelper)
    val biometricHelperState: StateFlow<BiometricHelper> = _biometricHelperState.asStateFlow()

    private val _shouldShowBiometric = MutableStateFlow(true)
    val shouldShowBiometric: StateFlow<Boolean> = _shouldShowBiometric.asStateFlow()

    private val _selectedClassroomId = MutableStateFlow<String?>(null)
    val selectedClassroomId: StateFlow<String?> = _selectedClassroomId.asStateFlow()

    private val _selectedTaskId = MutableStateFlow<String?>(null)
    val selectedTaskId: StateFlow<String?> = _selectedTaskId.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn()
        .map { isLoggedIn ->
            if (isLoggedIn) {
                // Check token validity when logged in state changes
                val authState = handleAuthStateUseCase.execute()
                val isAuthenticated = authState == AuthState.AUTHENTICATED
                if (isAuthenticated) {
                    _shouldShowBiometric.value = false // Disable biometric prompt once authenticated
                }
                isAuthenticated
            } else {
                _shouldShowBiometric.value = true // Reset biometric prompt state on logout
                false
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            val authState = handleAuthStateUseCase.execute()
            if (authState == AuthState.NEEDS_LOGIN) {
                authRepository.logout()
                _shouldShowBiometric.value = true // Reset biometric prompt state
            }
        }
    }

    fun setSelectedClassroom(classroomId: String) {
        _selectedClassroomId.value = classroomId
    }

    fun setSelectedTask(taskId: String) {
        _selectedTaskId.value = taskId
    }

    fun clearTaskSelection() {
        _selectedTaskId.value = null
    }

    fun clearClassroomSelection() {
        _selectedClassroomId.value = null
        _selectedTaskId.value = null
    }
}