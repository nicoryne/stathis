package citu.edu.stathis.mobile.features.classroom.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import citu.edu.stathis.mobile.core.theme.AppTheme
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.ClassroomViewModel
import citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.EnrollmentState

@Composable
fun EnrollClassroomScreen(
    navigateBack: () -> Unit,
    navigateToClassrooms: () -> Unit,
    viewModel: ClassroomViewModel = hiltViewModel()
) {
    val enrollmentState by viewModel.enrollmentState.collectAsState()
    var classroomCode by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    
    // Handle successful enrollment
    LaunchedEffect(enrollmentState) {
        if (enrollmentState is EnrollmentState.Success) {
            navigateToClassrooms()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top app bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            IconButton(onClick = navigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            Text(
                text = "Enroll in Classroom",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 24.dp)
            )
            
            Text(
                text = "Enter Classroom Code",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Ask your teacher for the classroom code",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .padding(horizontal = 32.dp)
            )
            
            // Classroom code input
            OutlinedTextField(
                value = classroomCode,
                onValueChange = { newValue -> 
                    // Normalize the code by removing spaces and converting to uppercase
                    val normalized = newValue.replace(" ", "").uppercase()
                    classroomCode = normalized
                },
                label = { Text("Classroom Code") },
                placeholder = { Text("e.g., ROOM-25-311") },
                singleLine = true,
                isError = enrollmentState is EnrollmentState.Error,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (classroomCode.isNotBlank()) {
                            viewModel.enrollInClassroom(classroomCode)
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            
            // Helper text
            Text(
                text = "Enter the code exactly as provided by your teacher",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp, end = 16.dp)
            )
            
            // Error message
            if (enrollmentState is EnrollmentState.Error) {
                Text(
                    text = (enrollmentState as EnrollmentState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Enroll button
            Button(
                onClick = { 
                    focusManager.clearFocus()
                    viewModel.enrollInClassroom(classroomCode)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                enabled = classroomCode.isNotBlank() && enrollmentState !is EnrollmentState.Enrolling
            ) {
                if (enrollmentState is EnrollmentState.Enrolling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Enroll")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EnrollClassroomScreenPreview() {
    AppTheme {
        EnrollClassroomScreen(
            navigateBack = {},
            navigateToClassrooms = {}
        )
    }
}
