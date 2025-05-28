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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import coil3.request.crossfade
import citu.edu.stathis.mobile.core.theme.BrandColors
import citu.edu.stathis.mobile.features.auth.data.models.UserResponseDTO
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

object R { object drawable { const val ic_profile_placeholder = 0 } }

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

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var birthdate by remember { mutableStateOf<String?>(null) }
    var currentPictureUrl by remember { mutableStateOf<String?>(null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedImageUrl by remember { mutableStateOf<String?>(null) }

    var schoolAttending by remember { mutableStateOf("") }
    var yearLevelInput by remember { mutableStateOf("") }
    var courseEnrolled by remember { mutableStateOf("") }

    var showImageConfirmDialog by remember { mutableStateOf(false) }
    var showDiscardChangesDialog by remember { mutableStateOf(false) }
    var isFormDirty by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            tempImageUri = it
            showImageConfirmDialog = true
            isFormDirty = true
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Success) {
            val profile = (uiState as ProfileUiState.Success).profile
            firstName = profile.firstName
            lastName = profile.lastName
            birthdate = profile.birthdate
            currentPictureUrl = profile.profilePictureUrl
            uploadedImageUrl = profile.profilePictureUrl
            schoolAttending = profile.school ?: ""
            yearLevelInput = profile.yearLevel?.toString() ?: ""
            courseEnrolled = profile.course ?: ""
            tempImageUri = null
            isFormDirty = false
        }
    }

    fun checkForChanges(currentProfile: UserResponseDTO?): Boolean {
        if (currentProfile == null) return tempImageUri != null
        return firstName != currentProfile.firstName ||
                lastName != currentProfile.lastName ||
                birthdate != currentProfile.birthdate ||
                (tempImageUri != null) ||
                schoolAttending != (currentProfile.school ?: "") ||
                yearLevelInput != (currentProfile.yearLevel?.toString() ?: "") ||
                courseEnrolled != (currentProfile.course ?: "")
    }

    LaunchedEffect(firstName, lastName, birthdate, tempImageUri, schoolAttending, yearLevelInput, courseEnrolled) {
        if (uiState is ProfileUiState.Success) {
            isFormDirty = checkForChanges((uiState as ProfileUiState.Success).profile)
        } else {
            isFormDirty = tempImageUri != null || firstName.isNotEmpty()
        }
    }

    LaunchedEffect(editState) {
        when (val state = editState) {
            is EditProfileUiState.Success -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Profile updated successfully!")
                    viewModel.resetEditState()
                }
            }
            is EditProfileUiState.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(state.message)
                    viewModel.resetEditState()
                }
            }
            else -> {}
        }
    }

    val handleBackPress = {
        if (isFormDirty) {
            showDiscardChangesDialog = true
        } else {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { handleBackPress() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                actions = {
                    if (isFormDirty) {
                        IconButton(onClick = { showDiscardChangesDialog = true }) {
                            Icon(Icons.Default.Close, "Discard Changes", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandColors.Purple)
                }
            }
            is ProfileUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text(state.message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadUserProfile() }, colors = ButtonDefaults.buttonColors(containerColor = BrandColors.Purple)) {
                            Text("Retry")
                        }
                    }
                }
            }
            is ProfileUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(tempImageUri ?: uploadedImageUrl ?: "")
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(120.dp).clip(CircleShape).border(2.dp, BrandColors.Purple, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier.align(Alignment.BottomEnd).size(40.dp).clip(CircleShape)
                                .background(BrandColors.Purple).clickable { imagePickerLauncher.launch("image/*") }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, "Change Picture", tint = Color.White)
                        }
                    }

                    if (tempImageUri != null || !uploadedImageUrl.isNullOrEmpty()) {
                        TextButton(
                            onClick = {
                                tempImageUri = null
                                uploadedImageUrl = null
                                isFormDirty = true
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.Delete, "Remove Picture", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("Remove Picture")
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth().animateCardElevation(isFormDirty),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, "Personal Info", tint = BrandColors.Purple)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Personal Information", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = birthdate ?: "",
                                onValueChange = { birthdate = it.ifBlank { null } },
                                label = { Text("Birthdate (YYYY-MM-DD)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                placeholder = { Text("YYYY-MM-DD")}
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth().animateCardElevation(isFormDirty),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.School, "Academic Info", tint = BrandColors.Purple)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Academic Information", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(value = schoolAttending, onValueChange = { schoolAttending = it }, label = { Text("School") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(value = courseEnrolled, onValueChange = { courseEnrolled = it }, label = { Text("Course") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = yearLevelInput,
                                onValueChange = { yearLevelInput = it },
                                label = { Text("Year Level") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { handleBackPress() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Cancel, "Cancel", modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val yearLevelInt = yearLevelInput.toIntOrNull()
                                viewModel.updateFullProfile(
                                    firstName = firstName,
                                    lastName = lastName,
                                    birthdate = birthdate,
                                    profilePictureUrl = uploadedImageUrl,
                                    school = schoolAttending.ifEmpty { null },
                                    yearLevel = yearLevelInt,
                                    course = courseEnrolled.ifEmpty { null }
                                )
                            },
                            enabled = isFormDirty && editState !is EditProfileUiState.Loading,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandColors.Purple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (editState is EditProfileUiState.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Check, "Save", modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Save")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        if (showImageConfirmDialog && tempImageUri != null) {
            AlertDialog(
                onDismissRequest = { showImageConfirmDialog = false },
                title = { Text("Update Profile Picture") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Do you want to update your profile picture?")
                        Spacer(modifier = Modifier.height(16.dp))
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(tempImageUri).crossfade(true).build(),
                            contentDescription = "Profile Picture Preview",
                            modifier = Modifier.size(150.dp).clip(CircleShape).border(2.dp, BrandColors.Purple, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            uploadedImageUrl = tempImageUri.toString()
                            currentPictureUrl = tempImageUri.toString()
                            showImageConfirmDialog = false
                        }
                    ) { Text("Use this image") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        tempImageUri = null
                        showImageConfirmDialog = false
                        if (uiState is ProfileUiState.Success) {
                            isFormDirty = checkForChanges((uiState as ProfileUiState.Success).profile)
                        }
                    }) { Text("Cancel") }
                }
            )
        }

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
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Discard") }
                },
                dismissButton = {
                    TextButton(onClick = { showDiscardChangesDialog = false }) { Text("Cancel") }
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
    return this.shadow(elevation = elevation, shape = RoundedCornerShape(16.dp))
}
