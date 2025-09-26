package citu.edu.stathis.mobile.features.tasks.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import citu.edu.stathis.mobile.features.tasks.data.model.QuizTemplate
import citu.edu.stathis.mobile.features.tasks.data.model.QuizQuestion

@Composable
fun QuizView(
    quiz: QuizTemplate,
    onSubmitScore: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAnswers by remember { mutableStateOf(mutableMapOf<String, Int>()) }

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = quiz.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(text = quiz.instruction, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))

        quiz.questions.forEach { q ->
            QuestionCard(
                question = q,
                selectedIndex = selectedAnswers[q.id],
                onSelect = { idx -> selectedAnswers[q.id] = idx }
            )
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                val score = quiz.questions.count { q -> selectedAnswers[q.id] == q.answer }
                val scaled = ((score.toFloat() / quiz.questions.size) * quiz.maxScore).toInt()
                onSubmitScore(scaled)
            },
            enabled = selectedAnswers.size == quiz.questions.size
        ) { Text("Submit Quiz") }
    }
}

@Composable
private fun QuestionCard(
    question: QuizQuestion,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit
) {
    Card { 
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "${question.questionNumber}. ${question.question}", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            question.options.forEachIndexed { idx, option ->
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(selected = selectedIndex == idx, onClick = { onSelect(idx) })
                    Spacer(Modifier.width(8.dp))
                    Text(option, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}


