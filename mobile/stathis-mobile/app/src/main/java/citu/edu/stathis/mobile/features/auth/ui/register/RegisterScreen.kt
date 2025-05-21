package citu.edu.stathis.mobile.features.auth.ui.register

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import citu.edu.stathis.mobile.features.auth.ui.components.AuthBackground
import citu.edu.stathis.mobile.features.auth.ui.components.AuthCard
import citu.edu.stathis.mobile.features.auth.ui.components.BackgroundDecorations
import citu.edu.stathis.mobile.features.auth.ui.components.GoogleSignInButton
import citu.edu.stathis.mobile.features.auth.ui.components.MicrosoftSignInButton
import citu.edu.stathis.mobile.features.auth.ui.components.PasswordRequirements
import citu.edu.stathis.mobile.features.auth.ui.components.PasswordStrength
import citu.edu.stathis.mobile.features.auth.ui.components.PasswordStrengthIndicator
import citu.edu.stathis.mobile.features.auth.ui.components.EmailVerificationDialog
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import citu.edu.stathis.mobile.core.theme.appColors

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val colors = appColors()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    val showVerificationDialog = remember { mutableStateOf(false) }
    val registeredEmail = remember { mutableStateOf("") }

    val registerAnimation by rememberLottieComposition(
        spec = LottieCompositionSpec.Url("https://assets9.lottiefiles.com/packages/lf20_z9ed2jna.json")
    )

    LaunchedEffect(key1 = true) {
        viewModel.events.collect { event ->
            when (event) {
                is RegisterEvent.NavigateToHome -> {
                    onNavigateToHome()
                }
                is RegisterEvent.NavigateToLogin -> {
                    onNavigateToLogin()
                }
                is RegisterEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is RegisterEvent.RegistrationSuccess -> {
                    registeredEmail.value = event.email
                    showVerificationDialog.value = true
                }
            }
        }
    }

    if (showVerificationDialog.value) {
        EmailVerificationDialog(
            email = registeredEmail.value,
            onDismiss = { showVerificationDialog.value = false },
            onResendEmail = { viewModel.onEvent(RegisterUiEvent.ResendVerificationEmail) },
            onContinue = {
                onNavigateToLogin()
            },
            isResendingEmail = state.isResendingEmail
        )
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
                Spacer(modifier = Modifier.height(60.dp))

                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 28.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sign up to get started",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Register animation
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LottieAnimation(
                        composition = registerAnimation,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                AuthCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // First Name
                        OutlinedTextField(
                            value = state.firstName,
                            onValueChange = { viewModel.onEvent(RegisterUiEvent.FirstNameChanged(it)) },
                            label = { Text("First Name") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "First Name",
                                    tint = colors.iconTint
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
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

                        // Last Name
                        OutlinedTextField(
                            value = state.lastName,
                            onValueChange = { viewModel.onEvent(RegisterUiEvent.LastNameChanged(it)) },
                            label = { Text("Last Name") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Last Name",
                                    tint = colors.iconTint
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
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

                        // Email
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = { viewModel.onEvent(RegisterUiEvent.EmailChanged(it)) },
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

                        // Password
                        OutlinedTextField(
                            value = state.password,
                            onValueChange = { viewModel.onEvent(RegisterUiEvent.PasswordChanged(it)) },
                            label = { Text("Password") },
                            trailingIcon = {
                                IconButton(
                                    onClick = { viewModel.onEvent(RegisterUiEvent.TogglePasswordVisibility) }
                                ) {
                                    Icon(
                                        imageVector = if (state.isPasswordVisible) {
                                            Icons.Default.Visibility
                                        } else {
                                            Icons.Default.VisibilityOff
                                        },
                                        contentDescription = if (state.isPasswordVisible) {
                                            "Hide password"
                                        } else {
                                            "Show password"
                                        },
                                        tint = colors.iconTint
                                    )
                                }
                            },
                            visualTransformation = if (state.isPasswordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        viewModel.onEvent(RegisterUiEvent.TogglePasswordRequirements)
                                    }
                                },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            singleLine = true,
                            isError = state.password.isNotEmpty() && state.passwordStrength == PasswordStrength.WEAK,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.textFieldBorder,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                cursorColor = colors.textFieldCursor,
                                errorBorderColor = MaterialTheme.colorScheme.error
                            )
                        )

                        PasswordStrengthIndicator(
                            password = state.password,
                            strength = state.passwordStrength
                        )

                        PasswordRequirements(
                            password = state.password,
                            isVisible = state.showPasswordRequirements
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Confirm Password
                        OutlinedTextField(
                            value = state.confirmPassword,
                            onValueChange = { viewModel.onEvent(RegisterUiEvent.ConfirmPasswordChanged(it)) },
                            label = { Text("Confirm Password") },
                            trailingIcon = {
                                IconButton(
                                    onClick = { viewModel.onEvent(RegisterUiEvent.ToggleConfirmPasswordVisibility) }
                                ) {
                                    Icon(
                                        imageVector = if (state.isConfirmPasswordVisible) {
                                            Icons.Default.Visibility
                                        } else {
                                            Icons.Default.VisibilityOff
                                        },
                                        contentDescription = if (state.isConfirmPasswordVisible) {
                                            "Hide password"
                                        } else {
                                            "Show password"
                                        },
                                        tint = colors.iconTint
                                    )
                                }
                            },
                            visualTransformation = if (state.isConfirmPasswordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.onEvent(RegisterUiEvent.Register)
                                }
                            ),
                            singleLine = true,
                            isError = state.confirmPassword.isNotEmpty() && !state.passwordsMatch,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.textFieldBorder,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                cursorColor = colors.textFieldCursor,
                                errorBorderColor = MaterialTheme.colorScheme.error
                            )
                        )

                        if (state.confirmPassword.isNotEmpty() && !state.passwordsMatch) {
                            Text(
                                text = "Passwords do not match",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(start = 16.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.onEvent(RegisterUiEvent.Register) },
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
                                    text = "Sign Up",
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Divider(
                                modifier = Modifier.weight(1f),
                                color = Color.LightGray
                            )
                            Text(
                                text = "OR",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Divider(
                                modifier = Modifier.weight(1f),
                                color = Color.LightGray
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        GoogleSignInButton(
                            onClick = { viewModel.onEvent(RegisterUiEvent.GoogleSignIn) },
                            isLoading = state.isGoogleLoading
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        MicrosoftSignInButton(
                            onClick = { viewModel.onEvent(RegisterUiEvent.MicrosoftSignIn) },
                            isLoading = state.isMicrosoftLoading
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = buildAnnotatedString {
                        append("Already have an account? ")
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF9334EA),
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("Sign In")
                        }
                    },
                    modifier = Modifier.clickable { viewModel.onEvent(RegisterUiEvent.NavigateToLogin) },
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
                    CircularProgressIndicator(
                        color = Color(0xFF9334EA)
                    )
                }
            }
        }
    }
}