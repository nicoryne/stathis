package citu.edu.stathis.mobile.features.classroom.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import citu.edu.stathis.mobile.features.classroom.data.model.Classroom
import citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.ClassroomViewModel
import java.time.OffsetDateTime
import citu.edu.stathis.mobile.core.theme.AppTheme
import citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.ClassroomsState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ClassroomsListScreen(
    navigateToClassroomDetail: (String) -> Unit,
    navigateToEnrollClassroom: () -> Unit,
    viewModel: ClassroomViewModel = hiltViewModel()
) {
    val classroomsState by viewModel.classroomsState.collectAsState()
    
    LaunchedEffect(key1 = true) {
        viewModel.loadStudentClassrooms()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "My Classrooms",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Add Classroom Button
        Button(
            onClick = { navigateToEnrollClassroom() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Enroll in classroom"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Enroll in Classroom")
        }
        
        // Content based on state
        when (classroomsState) {
            is ClassroomsState.Loading -> {
                LoadingIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
            is ClassroomsState.Error -> {
                val errorState = classroomsState as ClassroomsState.Error
                ErrorMessage(
                    message = errorState.message,
                    onRetryClick = { viewModel.loadStudentClassrooms() }
                )
            }
            is ClassroomsState.Success -> {
                val classrooms = (classroomsState as ClassroomsState.Success).classrooms
                if (classrooms.isEmpty()) {
                    EmptyClassroomsMessage(
                        message = "You're not enrolled in any classrooms yet",
                        subMessage = "Use the 'Enroll in Classroom' button to join a class",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                } else {
                    ClassroomsList(
                        classrooms = classrooms,
                        onClassroomClick = { classroom -> navigateToClassroomDetail(classroom.physicalId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
            is ClassroomsState.Empty -> {
                EmptyClassroomsMessage(
                    message = "No classrooms available",
                    subMessage = "You don't have access to any classrooms yet. Use the 'Enroll in Classroom' button to join a class using a code from your teacher.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
            else -> {
                // Handle any other states
                EmptyClassroomsMessage(
                    message = "No classrooms found",
                    subMessage = "Please try again or contact support if the issue persists",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
fun ClassroomsList(
    classrooms: List<Classroom>,
    onClassroomClick: (Classroom) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(classrooms) { classroom ->
            ClassroomCard(
                classroom = classroom,
                onClick = { onClassroomClick(classroom) }
            )
        }
    }
}

@Composable
fun ClassroomCard(
    classroom: Classroom,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Classroom Icon
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Classroom",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(30.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Classroom Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = classroom.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = classroom.description ?: "No description",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                // Teacher name
                if (classroom.teacherName != null) {
                    Text(
                        text = "Teacher: ${classroom.teacherName}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyClassroomsMessage(
    message: String = "You're not enrolled in any classrooms yet",
    subMessage: String = "Use the 'Enroll in Classroom' button to join a class",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(72.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = subMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text("Loading...", style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview
@Composable
fun ClassroomsListScreenPreview() {
    AppTheme {
        Surface {
            val sampleClassrooms = listOf(
                Classroom(
                    physicalId = "1",
                    name = "Mobile Development",
                    description = "Learn how to build mobile applications using Android and Kotlin",
                    classroomCode = "MOBILE101",
                    createdAt = OffsetDateTime.now(),
                    updatedAt = OffsetDateTime.now(),
                    teacherId = "1",
                    teacherName = "John Doe",
                    studentCount = 25,
                    isActive = true
                ),
                Classroom(
                    physicalId = "2",
                    name = "Web Development",
                    description = "Learn how to build web applications using React and Node.js",
                    classroomCode = "WEB101",
                    createdAt = OffsetDateTime.now(),
                    updatedAt = OffsetDateTime.now(),
                    teacherId = "2",
                    teacherName = "Jane Smith",
                    studentCount = 30,
                    isActive = true
                )
            )
            
            ClassroomsList(
                classrooms = sampleClassrooms,
                onClassroomClick = {}
            )
        }
    }
}
