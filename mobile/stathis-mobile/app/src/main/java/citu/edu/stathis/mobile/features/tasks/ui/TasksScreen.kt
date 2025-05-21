package citu.edu.stathis.mobile.features.tasks.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import citu.edu.stathis.mobile.core.theme.BrandColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(navController: NavHostController) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Today", "Upcoming", "Completed")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tasks",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Search tasks */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = { /* TODO: Filter tasks */ }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Add new task */ },
                containerColor = BrandColors.Purple,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Task progress summary
            TaskProgressSummary()

            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = BrandColors.Purple
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    )
                }
            }

            // Task list
            when (selectedTabIndex) {
                0 -> AllTasksList()
                1 -> TodayTasksList()
                2 -> UpcomingTasksList()
                3 -> CompletedTasksList()
            }
        }
    }
}

@Composable
fun TaskProgressSummary() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = "Tasks",
                    tint = BrandColors.Purple,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = "Task Progress",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "May 7, 2025",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Task progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "8/15 completed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "53%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandColors.Purple
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = 0.53f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = BrandColors.Purple,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TaskStatItem(
                    count = "5",
                    label = "Remaining",
                    color = Color(0xFFFFA726) // Orange
                )

                TaskStatItem(
                    count = "2",
                    label = "Due Today",
                    color = Color(0xFFF44336) // Red
                )

                TaskStatItem(
                    count = "8",
                    label = "Completed",
                    color = Color(0xFF4CAF50) // Green
                )
            }
        }
    }
}

@Composable
fun TaskStatItem(
    count: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AllTasksList() {
    val tasks = remember {
        listOf(
            Task(
                id = "1",
                title = "Complete Posture Analysis",
                description = "Analyze your posture for 5 minutes",
                dueDate = "Today",
                priority = TaskPriority.HIGH,
                isCompleted = true,
                category = "Posture"
            ),
            Task(
                id = "2",
                title = "Submit Assignment #3",
                description = "Complete and submit the programming assignment",
                dueDate = "Today",
                priority = TaskPriority.HIGH,
                isCompleted = true,
                category = "Academic"
            ),
            Task(
                id = "3",
                title = "Practice Good Posture (30 min)",
                description = "Maintain good posture while studying",
                dueDate = "Today",
                priority = TaskPriority.MEDIUM,
                isCompleted = true,
                category = "Posture"
            ),
            Task(
                id = "4",
                title = "Read Chapter 5",
                description = "Read and take notes on Chapter 5",
                dueDate = "Today",
                priority = TaskPriority.MEDIUM,
                isCompleted = false,
                category = "Academic"
            ),
            Task(
                id = "5",
                title = "Team Meeting",
                description = "Discuss project progress with team",
                dueDate = "Tomorrow",
                priority = TaskPriority.HIGH,
                isCompleted = false,
                category = "Academic"
            ),
            Task(
                id = "6",
                title = "Posture Exercise Routine",
                description = "Complete the daily posture exercise routine",
                dueDate = "Tomorrow",
                priority = TaskPriority.MEDIUM,
                isCompleted = false,
                category = "Posture"
            ),
            Task(
                id = "7",
                title = "Review Lecture Notes",
                description = "Review notes from today's lecture",
                dueDate = "May 9",
                priority = TaskPriority.LOW,
                isCompleted = false,
                category = "Academic"
            ),
            Task(
                id = "8",
                title = "Weekly Posture Assessment",
                description = "Complete the weekly posture assessment",
                dueDate = "May 10",
                priority = TaskPriority.HIGH,
                isCompleted = false,
                category = "Posture"
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Text(
                text = "Today",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        items(tasks.filter { it.dueDate == "Today" }) { task ->
            TaskItem(task = task)
        }

        item {
            Text(
                text = "Tomorrow",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )
        }

        items(tasks.filter { it.dueDate == "Tomorrow" }) { task ->
            TaskItem(task = task)
        }

        item {
            Text(
                text = "Upcoming",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )
        }

        items(tasks.filter { it.dueDate != "Today" && it.dueDate != "Tomorrow" }) { task ->
            TaskItem(task = task)
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun TodayTasksList() {
    val tasks = remember {
        listOf(
            Task(
                id = "1",
                title = "Complete Posture Analysis",
                description = "Analyze your posture for 5 minutes",
                dueDate = "Today",
                priority = TaskPriority.HIGH,
                isCompleted = true,
                category = "Posture"
            ),
            Task(
                id = "2",
                title = "Submit Assignment #3",
                description = "Complete and submit the programming assignment",
                dueDate = "Today",
                priority = TaskPriority.HIGH,
                isCompleted = true,
                category = "Academic"
            ),
            Task(
                id = "3",
                title = "Practice Good Posture (30 min)",
                description = "Maintain good posture while studying",
                dueDate = "Today",
                priority = TaskPriority.MEDIUM,
                isCompleted = true,
                category = "Posture"
            ),
            Task(
                id = "4",
                title = "Read Chapter 5",
                description = "Read and take notes on Chapter 5",
                dueDate = "Today",
                priority = TaskPriority.MEDIUM,
                isCompleted = false,
                category = "Academic"
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Text(
                text = "Due Today",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        items(tasks.filter { !it.isCompleted }) { task ->
            TaskItem(task = task)
        }

        item {
            Text(
                text = "Completed Today",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )
        }

        items(tasks.filter { it.isCompleted }) { task ->
            TaskItem(task = task)
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun UpcomingTasksList() {
    val tasks = remember {
        listOf(
            Task(
                id = "5",
                title = "Team Meeting",
                description = "Discuss project progress with team",
                dueDate = "Tomorrow",
                priority = TaskPriority.HIGH,
                isCompleted = false,
                category = "Academic"
            ),
            Task(
                id = "6",
                title = "Posture Exercise Routine",
                description = "Complete the daily posture exercise routine",
                dueDate = "Tomorrow",
                priority = TaskPriority.MEDIUM,
                isCompleted = false,
                category = "Posture"
            ),
            Task(
                id = "7",
                title = "Review Lecture Notes",
                description = "Review notes from today's lecture",
                dueDate = "May 9",
                priority = TaskPriority.LOW,
                isCompleted = false,
                category = "Academic"
            ),
            Task(
                id = "8",
                title = "Weekly Posture Assessment",
                description = "Complete the weekly posture assessment",
                dueDate = "May 10",
                priority = TaskPriority.HIGH,
                isCompleted = false,
                category = "Posture"
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Text(
                text = "Tomorrow",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        items(tasks.filter { it.dueDate == "Tomorrow" }) { task ->
            TaskItem(task = task)
        }

        item {
            Text(
                text = "This Week",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )
        }

        items(tasks.filter { it.dueDate != "Tomorrow" }) { task ->
            TaskItem(task = task)
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun CompletedTasksList() {
    val tasks = remember {
        listOf(
            Task(
                id = "1",
                title = "Complete Posture Analysis",
                description = "Analyze your posture for 5 minutes",
                dueDate = "Today",
                priority = TaskPriority.HIGH,
                isCompleted = true,
                category = "Posture"
            ),
            Task(
                id = "2",
                title = "Submit Assignment #3",
                description = "Complete and submit the programming assignment",
                dueDate = "Today",
                priority = TaskPriority.HIGH,
                isCompleted = true,
                category = "Academic"
            ),
            Task(
                id = "3",
                title = "Practice Good Posture (30 min)",
                description = "Maintain good posture while studying",
                dueDate = "Today",
                priority = TaskPriority.MEDIUM,
                isCompleted = true,
                category = "Posture"
            ),
            Task(
                id = "9",
                title = "Complete Quiz #2",
                description = "Complete online quiz for Chapter 4",
                dueDate = "Yesterday",
                priority = TaskPriority.HIGH,
                isCompleted = true,
                category = "Academic"
            ),
            Task(
                id = "10",
                title = "Posture Tracking Setup",
                description = "Set up posture tracking on your device",
                dueDate = "May 5",
                priority = TaskPriority.HIGH,
                isCompleted = true,
                category = "Posture"
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Text(
                text = "Today",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        items(tasks.filter { it.dueDate == "Today" && it.isCompleted }) { task ->
            TaskItem(task = task)
        }

        item {
            Text(
                text = "Earlier",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )
        }

        items(tasks.filter { it.dueDate != "Today" && it.isCompleted }) { task ->
            TaskItem(task = task)
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(task: Task) {
    var isChecked by remember { mutableStateOf(task.isCompleted) }

    Card(
        onClick = { /* TODO: Navigate to task details */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (task.priority) {
                TaskPriority.HIGH -> BrandColors.Purple.copy(alpha = 0.05f)
                TaskPriority.MEDIUM -> BrandColors.Teal.copy(alpha = 0.05f)
                TaskPriority.LOW -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { isChecked = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = when (task.priority) {
                        TaskPriority.HIGH -> BrandColors.Purple
                        TaskPriority.MEDIUM -> BrandColors.Teal
                        TaskPriority.LOW -> MaterialTheme.colorScheme.primary
                    },
                    uncheckedColor = when (task.priority) {
                        TaskPriority.HIGH -> BrandColors.Purple.copy(alpha = 0.6f)
                        TaskPriority.MEDIUM -> BrandColors.Teal.copy(alpha = 0.6f)
                        TaskPriority.LOW -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    }
                )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isChecked) FontWeight.Normal else FontWeight.Medium,
                        textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (isChecked)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Due Date",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = task.dueDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            task.dueDate == "Today" && !isChecked -> Color(0xFFF44336) // Red
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (task.category) {
                                "Posture" -> BrandColors.Purple.copy(alpha = 0.1f)
                                "Academic" -> BrandColors.Teal.copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = task.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = when (task.category) {
                            "Posture" -> BrandColors.Purple
                            "Academic" -> BrandColors.Teal
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val dueDate: String,
    val priority: TaskPriority,
    val isCompleted: Boolean,
    val category: String
)

enum class TaskPriority {
    HIGH, MEDIUM, LOW
}