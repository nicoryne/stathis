package citu.edu.stathis.mobile.features.profile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@Composable
fun EditProfileScreen(navController: NavHostController, viewModel: EditProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var birthdate by remember { mutableStateOf("") }
    var profilePictureUrl by remember { mutableStateOf<String?>(null) }

    var school by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var yearLevelText by remember { mutableStateOf("") }

    LaunchedEffect(state.success) {
        if (state.success) navController.popBackStack()
    }

    Scaffold(
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
                Text(text = "Edit profile", style = MaterialTheme.typography.headlineSmall)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Basic info
            Text(text = "Basic information", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = birthdate, onValueChange = { birthdate = it }, label = { Text("Birthdate (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

            // Student info
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Student details", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = school, onValueChange = { school = it }, label = { Text("School") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = course, onValueChange = { course = it }, label = { Text("Course") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = yearLevelText, onValueChange = { yearLevelText = it.filter { ch -> ch.isDigit() } }, label = { Text("Year level") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

            if (state.errorMessage != null) {
                Text(text = state.errorMessage ?: "", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.updateUser(
                        firstName = firstName,
                        lastName = lastName,
                        birthdate = birthdate.ifBlank { null },
                        profilePictureUrl = profilePictureUrl
                    )
                    viewModel.updateStudent(
                        school = school.ifBlank { null },
                        course = course.ifBlank { null },
                        yearLevel = yearLevelText.toIntOrNull()
                    )
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text("Save changes") }
        }
    }
}



