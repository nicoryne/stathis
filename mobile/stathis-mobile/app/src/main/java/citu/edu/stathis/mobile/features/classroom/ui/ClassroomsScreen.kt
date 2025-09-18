package citu.edu.stathis.mobile.features.classroom.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import citu.edu.stathis.mobile.core.theme.BrandColors
import citu.edu.stathis.mobile.features.classroom.data.model.Classroom
import citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.ClassroomViewModel
import citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.ClassroomsState
import citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.EnrollmentState
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomsScreen(
    navController: NavHostController,
    viewModel: ClassroomViewModel = hiltViewModel()
) {
    val classroomsState by viewModel.classroomsState.collectAsState()
    val enrollmentState by viewModel.enrollmentState.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var showEnrollDialog by remember { mutableStateOf(false) }
    
    // Load classrooms when the screen is first displayed
    LaunchedEffect(key1 = true) {
        viewModel.loadStudentClassrooms()
    }
    
    // Handle enrollment state changes
    LaunchedEffect(key1 = enrollmentState) {
        when (enrollmentState) {
            is EnrollmentState.Success -> {
                val classroom = (enrollmentState as EnrollmentState.Success).classroom
                scope.launch {
                    snackbarHostState.showSnackbar("Enrolled in ${classroom.name}")
                }
                viewModel.resetEnrollmentState()
            }
            is EnrollmentState.Error -> {
                val message = (enrollmentState as EnrollmentState.Error).message
                scope.launch {
                    snackbarHostState.showSnackbar("Error: $message")
                }
                viewModel.resetEnrollmentState()
            }
            else -> { /* No action needed for other states */ }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Classrooms",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showEnrollDialog = true },
                containerColor = BrandColors.Purple
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Enroll in Classroom",
                    tint = Color.White
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (classroomsState) {
                is ClassroomsState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = BrandColors.Purple
                    )
                }
                is ClassroomsState.Success -> {
                    val classrooms = (classroomsState as ClassroomsState.Success).classrooms
                    ClassroomsList(
                        classrooms = classrooms,
                        onClassroomClick = { classroom ->
                            navController.navigate("classroom_detail/${classroom.physicalId}")
                        }
                    )
                }
                is ClassroomsState.Empty -> {
                    EmptyClassroomsMessage(
                        onEnrollClick = { showEnrollDialog = true }
                    )
                }
                is ClassroomsState.Error -> {
                    val message = (classroomsState as ClassroomsState.Error).message
                    ErrorMessage(
                        message = message,
                        onRetryClick = { viewModel.loadStudentClassrooms() }
                    )
                }
            }
        }
    }
    
    // Enrollment dialog
    if (showEnrollDialog) {
        EnrollmentDialog(
            isEnrolling = enrollmentState is EnrollmentState.Enrolling,
            onDismiss = { showEnrollDialog = false },
            onEnroll = { code ->
                showEnrollDialog = false
                viewModel.enrollInClassroom(code)
            }
        )
    }
}

@Composable
fun ClassroomsList(
    classrooms: List<Classroom>,
    onClassroomClick: (Classroom) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(classrooms) { classroom ->
            ClassroomItem(
                classroom = classroom,
                onClick = { onClassroomClick(classroom) }
            )
        }
    }
}

@Composable
fun ClassroomItem(
    classroom: Classroom,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            if (classroom.isActive) Color.Green else Color.Gray
                        )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Classroom name
                Text(
                    text = classroom.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description
            Text(
                text = classroom.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Divider()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Classroom details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Teacher
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Teacher",
                        tint = BrandColors.Purple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = classroom.teacherName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Students count
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Students",
                        tint = BrandColors.Purple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${classroom.studentCount} students",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Code
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Code",
                        tint = BrandColors.Purple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Code: ${classroom.classroomCode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Created date
            Text(
                text = "Created: ${classroom.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun EmptyClassroomsMessage(
    onEnrollClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.School,
            contentDescription = "No Classrooms",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Classrooms Yet",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Enroll in a classroom to start your learning journey",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onEnrollClick,
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Enroll"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Enroll in Classroom")
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRetryClick,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Retry")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentDialog(
    isEnrolling: Boolean,
    onDismiss: () -> Unit,
    onEnroll: (String) -> Unit
) {
    var classroomCode by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Enroll in Classroom",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter the classroom code provided by your teacher",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = classroomCode,
                    onValueChange = { classroomCode = it },
                    label = { Text("Classroom Code") },
                    singleLine = true,
                    isError = classroomCode.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (isEnrolling) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = BrandColors.Purple
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onEnroll(classroomCode) },
                enabled = classroomCode.isNotBlank() && !isEnrolling
            ) {
                Text("Enroll")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isEnrolling
            ) {
                Text("Cancel")
            }
        }
    )
}
