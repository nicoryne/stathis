package citu.edu.stathis.mobile.features.profile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

private enum class Step { EMAIL, PASSWORD, NAME_TERMS }

@Composable
fun CreateProfileScreen(navController: NavHostController, viewModel: CreateProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(false) }

    var step by remember { mutableStateOf(Step.EMAIL) }

    // Snackbar host state for feedback
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.success) {
        if (state.success) {
            snackbarHostState.showSnackbar("Account created! Welcome to Stathis.")
            // Go to profile tab after successful auto-login
            navController.navigate("profile") {
                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Create Profile",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                val stepIndex = when (step) { Step.EMAIL -> 1; Step.PASSWORD -> 2; Step.NAME_TERMS -> 3 }
                Text(text = "$stepIndex/3", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val emailValid = remember(email) { android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() }
            val passwordValid = remember(password) { password.length >= 8 }
            val hasUpper = remember(password) { password.any { it.isUpperCase() } }
            val hasLower = remember(password) { password.any { it.isLowerCase() } }
            val hasDigit = remember(password) { password.any { it.isDigit() } }
            val hasSymbol = remember(password) { password.any { !it.isLetterOrDigit() } }
            val passwordsMatch = remember(password, confirmPassword) { password == confirmPassword }

            when (step) {
                Step.EMAIL -> {
                    Text(text = "What's your email address", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email address") },
                        singleLine = true,
                        isError = email.isNotBlank() && !emailValid,
                        supportingText = {
                            if (email.isNotBlank() && !emailValid) Text("Enter a valid email (name@domain.com)")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Step.PASSWORD -> {
                    Text(text = "Create your password", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password (8+ characters)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = password.isNotBlank() && !(passwordValid && hasUpper && hasLower && hasDigit && hasSymbol),
                        supportingText = {
                            Text(
                                "8+ chars, upper, lower, number, symbol",
                                color = if (passwordValid && hasUpper && hasLower && hasDigit && hasSymbol) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = confirmPassword.isNotBlank() && !passwordsMatch,
                        supportingText = { if (confirmPassword.isNotBlank() && !passwordsMatch) Text("Passwords do not match") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Step.NAME_TERMS -> {
                    Text(text = "Tell us about you", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)
                    OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Checkbox(checked = agreedToTerms, onCheckedChange = { agreedToTerms = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "I agree to the ")
                        TextButton(onClick = { navController.navigate("terms") }) { Text("Terms") }
                        Text(text = " and ")
                        TextButton(onClick = { navController.navigate("privacy") }) { Text("Privacy Policy") }
                    }
                }
            }

            if (state.errorMessage != null) {
                Text(text = state.errorMessage ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            val canContinue = when (step) {
                Step.EMAIL -> emailValid
                Step.PASSWORD -> (passwordValid && hasUpper && hasLower && hasDigit && hasSymbol && passwordsMatch)
                Step.NAME_TERMS -> (firstName.isNotBlank() && lastName.isNotBlank() && agreedToTerms)
            }

            Button(
                onClick = {
                    when (step) {
                        Step.EMAIL -> if (canContinue) step = Step.PASSWORD
                        Step.PASSWORD -> if (canContinue) step = Step.NAME_TERMS
                        Step.NAME_TERMS -> if (canContinue) {
                            viewModel.registerStudent(email = email, password = password, firstName = firstName, lastName = lastName)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(48.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp,
                    focusedElevation = 8.dp
                ),
                enabled = !state.isSubmitting && canContinue
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                val label = when (step) {
                    Step.EMAIL, Step.PASSWORD -> "CONTINUE"
                    Step.NAME_TERMS -> "CREATE ACCOUNT"
                }
                Text(text = label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold))
            }
        }
    }
}


