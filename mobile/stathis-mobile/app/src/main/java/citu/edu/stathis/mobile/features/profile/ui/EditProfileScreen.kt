package citu.edu.stathis.mobile.features.profile.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import citu.edu.stathis.mobile.core.theme.BrandColors
import coil3.request.crossfade
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val editState by viewModel.editState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // State for form fields
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var schoolAttending by remember { mutableStateOf("") }
    var yearLevel by remember { mutableStateOf<Short>(1) }
    var courseEnrolled by remember { mutableStateOf("") }

    // State for profile picture
    var currentPictureUrl by remember { mutableStateOf<String?>(null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageConfirmDialog by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var showDiscardChangesDialog by remember { mutableStateOf(false) }

    // Animation for the save button
    val saveButtonElevation by animateDpAsState(
        targetValue = if (hasUnsavedChanges) 8.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "saveButtonElevation"
    )

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            tempImageUri = it
            hasUnsavedChanges = true
            showImageConfirmDialog = true
        }
    }

    // Initialize form fields when profile is loaded
    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Success) {
            val profile = (uiState as ProfileUiState.Success).profile
            firstName = profile.firstName
            lastName = profile.lastName
            schoolAttending = profile.schoolAttending ?: ""
            yearLevel = profile.yearLevel ?: 1
            courseEnrolled = profile.courseEnrolled ?: ""
            currentPictureUrl = profile.pictureUrl
            hasUnsavedChanges = false
        }
    }

    // Check for form changes
    val formChanged by remember(firstName, lastName, schoolAttending, yearLevel, courseEnrolled, tempImageUri) {
        derivedStateOf {
            if (uiState is ProfileUiState.Success) {
                val profile = (uiState as ProfileUiState.Success).profile
                firstName != profile.firstName ||
                        lastName != profile.lastName ||
                        schoolAttending != (profile.schoolAttending ?: "") ||
                        yearLevel != (profile.yearLevel ?: 1) ||
                        courseEnrolled != (profile.courseEnrolled ?: "") ||
                        tempImageUri != null
            } else {
                false
            }
        }
    }

    // Update hasUnsavedChanges based on form changes
    LaunchedEffect(formChanged) {
        hasUnsavedChanges = formChanged
    }

    // Handle edit state changes
    LaunchedEffect(editState) {
        when (editState) {
            is EditProfileUiState.Success -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Profile updated successfully!")
                    viewModel.resetEditState()
                    tempImageUri = null
                    hasUnsavedChanges = false
                }
            }
            is EditProfileUiState.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Oh no! Something went wrong.")
                    viewModel.resetEditState()
                }
            }
            else -> {}
        }
    }

    // Back press handling
    val handleBackPress = {
        if (hasUnsavedChanges) {
            showDiscardChangesDialog = true
        } else {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Profile",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { handleBackPress() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    if (hasUnsavedChanges) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically()
                        ) {
                            IconButton(
                                onClick = { showDiscardChangesDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Discard Changes",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (uiState) {
            is ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandColors.Purple)
                }
            }

            is ProfileUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = (uiState as ProfileUiState.Error).message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.loadUserProfile() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandColors.Purple
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            is ProfileUiState.Success -> {
                val profile = (uiState as ProfileUiState.Success).profile

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile picture
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Profile picture or placeholder
                        if (tempImageUri != null) {
                            // Show temporary selected image
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(tempImageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Picture Preview",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, BrandColors.Purple, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else if (currentPictureUrl != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(currentPictureUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, BrandColors.Purple, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Default profile picture
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                BrandColors.Purple,
                                                BrandColors.Teal
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = firstName.firstOrNull()?.toString() + lastName.firstOrNull()?.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White
                                )
                            }
                        }

                        // Camera icon for changing picture
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrandColors.Purple)
                                .clickable { imagePickerLauncher.launch("image/*") }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change Picture",
                                tint = Color.White
                            )
                        }
                    }

                    // Remove picture button
                    AnimatedVisibility(
                        visible = currentPictureUrl != null || tempImageUri != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        TextButton(
                            onClick = {
                                if (tempImageUri != null) {
                                    tempImageUri = null
                                    hasUnsavedChanges = formChanged
                                } else if (currentPictureUrl != null) {
                                    viewModel.deleteProfilePicture()
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove Picture",
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.size(4.dp))

                            Text("Remove Picture")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Personal Information Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateCardElevation(hasUnsavedChanges),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Personal Info",
                                    tint = BrandColors.Purple
                                )

                                Spacer(modifier = Modifier.size(8.dp))

                                Text(
                                    text = "Personal Information",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = firstName,
                                onValueChange = {
                                    firstName = it
                                    hasUnsavedChanges = true
                                },
                                label = { Text("First Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = lastName,
                                onValueChange = {
                                    lastName = it
                                    hasUnsavedChanges = true
                                },
                                label = { Text("Last Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Academic Information Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateCardElevation(hasUnsavedChanges),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "Academic Info",
                                    tint = BrandColors.Purple
                                )

                                Spacer(modifier = Modifier.size(8.dp))

                                Text(
                                    text = "Academic Information",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = schoolAttending,
                                onValueChange = {
                                    schoolAttending = it
                                    hasUnsavedChanges = true
                                },
                                label = { Text("School") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = courseEnrolled,
                                onValueChange = {
                                    courseEnrolled = it
                                    hasUnsavedChanges = true
                                },
                                label = { Text("Course") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = yearLevel.toString(),
                                onValueChange = {
                                    val parsed = it.toShortOrNull()
                                    if (parsed != null) {
                                        yearLevel = parsed
                                        hasUnsavedChanges = true
                                    }
                                },
                                label = { Text("Year Level") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Cancel button
                        OutlinedButton(
                            onClick = { handleBackPress() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Cancel",
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.size(8.dp))

                            Text("Cancel")
                        }

                        // Save button
                        Button(
                            onClick = {
                                viewModel.updateUserProfile(
                                    firstName = firstName,
                                    lastName = lastName,
                                    schoolAttending = schoolAttending.ifEmpty { null },
                                    yearLevel = yearLevel,
                                    courseEnrolled = courseEnrolled.ifEmpty { null }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandColors.Purple
                            ),
                            enabled = hasUnsavedChanges && editState !is EditProfileUiState.Loading,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (editState is EditProfileUiState.Loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Save",
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.size(8.dp))

                                Text("Save")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp)) // Extra space for FAB
                }
            }
        }

        // Image confirmation dialog
        if (showImageConfirmDialog && tempImageUri != null) {
            AlertDialog(
                onDismissRequest = {
                    showImageConfirmDialog = false
                },
                title = { Text("Update Profile Picture") },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Do you want to update your profile picture?")

                        Spacer(modifier = Modifier.height(16.dp))

                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(tempImageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile Picture Preview",
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                                .border(2.dp, BrandColors.Purple, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            tempImageUri?.let {
                                viewModel.uploadProfilePicture(it)
                                showImageConfirmDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandColors.Purple
                        )
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            tempImageUri = null
                            showImageConfirmDialog = false
                            hasUnsavedChanges = formChanged
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Discard changes dialog
        if (showDiscardChangesDialog) {
            AlertDialog(
                onDismissRequest = { showDiscardChangesDialog = false },
                title = { Text("Discard Changes") },
                text = { Text("You have unsaved changes. Are you sure you want to discard them?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDiscardChangesDialog = false
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Discard")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDiscardChangesDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun Modifier.animateCardElevation(hasChanges: Boolean): Modifier {
    val elevation by animateDpAsState(
        targetValue = if (hasChanges) 4.dp else 2.dp,
        animationSpec = tween(durationMillis = 300),
        label = "cardElevation"
    )

    return this.shadow(
        elevation = elevation,
        shape = RoundedCornerShape(16.dp)
    )
}