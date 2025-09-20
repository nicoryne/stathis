package citu.edu.stathis.mobile.features.auth.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import citu.edu.stathis.mobile.core.auth.BiometricHelper
import citu.edu.stathis.mobile.core.theme.appColors
import citu.edu.stathis.mobile.features.auth.data.models.BiometricState
import citu.edu.stathis.mobile.features.auth.ui.components.AuthBackground
import citu.edu.stathis.mobile.features.auth.ui.components.AuthCard
import citu.edu.stathis.mobile.features.auth.ui.components.BackgroundDecorations
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
    biometricHelper: BiometricHelper
) {
    val colors = appColors()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    val biometricState by viewModel.biometricState.collectAsState()
    val context = LocalContext.current
    var shouldShowBiometricPrompt by remember { mutableStateOf(false) }

    val loginAnimation by rememberLottieComposition(
        spec = LottieCompositionSpec.Url("https://assets5.lottiefiles.com/packages/lf20_mjlh3hcy.json")
    )

    LaunchedEffect(biometricState) {
        shouldShowBiometricPrompt = biometricState == BiometricState.Available
    }

    if (shouldShowBiometricPrompt) {
        LaunchedEffect(Unit) {
            biometricHelper.showBiometricPrompt(
                activity = context as FragmentActivity,
                onSuccess = {
                    viewModel.onEvent(LoginUiEvent.BiometricLogin)
                    shouldShowBiometricPrompt = false
                },
                onError = { _ ->
                    shouldShowBiometricPrompt = false
                },
                onFailed = {
                    shouldShowBiometricPrompt = false
                }
            )
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.NavigateToHome -> onNavigateToHome()
                is LoginEvent.NavigateToRegister -> onNavigateToRegister()
                is LoginEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        AuthBackground {
            BackgroundDecorations()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(80.dp))

                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 28.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sign in to continue",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier.size(160.dp).padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LottieAnimation(
                        composition = loginAnimation,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                AuthCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = { viewModel.onEvent(LoginUiEvent.EmailChanged(it)) },
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = colors.iconTint
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.textFieldBorder,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                cursorColor = colors.textFieldCursor
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = state.password,
                            onValueChange = { viewModel.onEvent(LoginUiEvent.PasswordChanged(it)) },
                            label = { Text("Password") },
                            trailingIcon = {
                                IconButton(
                                    onClick = { viewModel.onEvent(LoginUiEvent.TogglePasswordVisibility) }
                                ) {
                                    Icon(
                                        imageVector = if (state.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (state.isPasswordVisible) "Hide password" else "Show password",
                                        tint = colors.iconTint
                                    )
                                }
                            },
                            visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.onEvent(LoginUiEvent.Login)
                                }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.textFieldBorder,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                cursorColor = colors.textFieldCursor
                            )
                        )


                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.onEvent(LoginUiEvent.Login) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isLoading,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9334EA),
                                contentColor = Color.White
                            )
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "Sign In",
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = buildAnnotatedString {
                        append("Don't have an account? ")
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF9334EA),
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("Sign Up")
                        }
                    },
                    modifier = Modifier.clickable { viewModel.onEvent(LoginUiEvent.NavigateToRegister) },
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(32.dp))
            }

            AnimatedVisibility(
                visible = state.isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF9334EA))
                }
            }
        }
    }
}