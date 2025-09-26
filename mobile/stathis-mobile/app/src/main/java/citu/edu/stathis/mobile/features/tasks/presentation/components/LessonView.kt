package citu.edu.stathis.mobile.features.tasks.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import citu.edu.stathis.mobile.features.tasks.data.model.LessonTemplate
import citu.edu.stathis.mobile.features.tasks.data.model.LessonPage

@Composable
fun LessonView(
    lesson: LessonTemplate,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(text = lesson.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(text = lesson.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))

        lesson.pages.sortedBy { it.pageNumber }.forEach { page ->
            Text(text = "${page.pageNumber}. ${page.subtitle}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(text = page.paragraph, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
        }
    }
}


