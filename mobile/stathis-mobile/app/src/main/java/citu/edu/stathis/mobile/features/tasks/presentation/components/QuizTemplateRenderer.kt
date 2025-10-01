package citu.edu.stathis.mobile.features.tasks.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import citu.edu.stathis.mobile.features.tasks.data.model.QuizTemplate
import citu.edu.stathis.mobile.features.tasks.data.model.QuizQuestion
import citu.edu.stathis.mobile.features.tasks.data.model.QuizSubmission
import citu.edu.stathis.mobile.features.tasks.data.model.QuizAnswer
import citu.edu.stathis.mobile.features.tasks.data.model.QuizResult
import citu.edu.stathis.mobile.features.tasks.data.model.QuizAnswerResult

@Composable
fun QuizTemplateRenderer(
    template: QuizTemplate,
    onSubmit: (QuizSubmission) -> Unit,
    onBackToTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    val selectedAnswers = remember { mutableStateMapOf<String, Int>() }
    var isSubmitted by remember { mutableStateOf(false) }
    var quizResult by remember { mutableStateOf<QuizResult?>(null) }
    
    val questions = template.content.questions
    val currentQuestion = questions.getOrNull(currentQuestionIndex)
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Progress Header
        QuizProgressHeader(
            currentQuestion = currentQuestionIndex + 1,
            totalQuestions = questions.size,
            title = template.title,
            instruction = template.instruction,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (isSubmitted && quizResult != null) {
                QuizResultScreen(
                    result = quizResult!!,
                    onRetake = {
                        isSubmitted = false
                        currentQuestionIndex = 0
                        selectedAnswers.clear()
                        quizResult = null
                    },
                    onBackToTask = onBackToTask,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (currentQuestion != null) {
                QuizQuestionContent(
                    question = currentQuestion,
                    selectedAnswer = selectedAnswers[currentQuestion.id],
                    onAnswerSelected = { answerIndex ->
                        selectedAnswers[currentQuestion.id] = answerIndex
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Navigation Footer
        if (!isSubmitted) {
            QuizNavigationFooter(
                currentQuestion = currentQuestionIndex,
                totalQuestions = questions.size,
                hasAnswer = currentQuestion?.id?.let { selectedAnswers.containsKey(it) } == true,
                onPrevious = { 
                    if (currentQuestionIndex > 0) {
                        currentQuestionIndex--
                    }
                },
                onNext = { 
                    if (currentQuestionIndex < questions.size - 1) {
                        currentQuestionIndex++
                    }
                },
                onSubmit = {
                    // Calculate quiz result
                    val answers = questions.map { question ->
                        QuizAnswer(
                            questionId = question.id,
                            selectedAnswer = selectedAnswers[question.id] ?: -1
                        )
                    }
                    
                    val submission = QuizSubmission(
                        taskId = "", // Will be set by parent
                        templateId = template.physicalId,
                        answers = answers
                    )
                    
                    // Calculate result
                    val correctAnswers = answers.count { answer ->
                        val question = questions.find { it.id == answer.questionId }
                        question?.answer == answer.selectedAnswer
                    }
                    
                    val result = QuizResult(
                        score = correctAnswers,
                        maxScore = questions.size,
                        correctAnswers = correctAnswers,
                        totalQuestions = questions.size,
                        percentage = (correctAnswers.toFloat() / questions.size) * 100f,
                        answers = answers.map { answer ->
                            val question = questions.find { it.id == answer.questionId }!!
                            QuizAnswerResult(
                                questionId = answer.questionId,
                                question = question.question,
                                selectedAnswer = answer.selectedAnswer,
                                correctAnswer = question.answer,
                                isCorrect = question.answer == answer.selectedAnswer,
                                options = question.options
                            )
                        }
                    )
                    
                    quizResult = result
                    isSubmitted = true
                    onSubmit(submission)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun QuizProgressHeader(
    currentQuestion: Int,
    totalQuestions: Int,
    title: String,
    instruction: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Instruction
            Text(
                text = instruction,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Bar
            val progress = if (totalQuestions > 0) currentQuestion.toFloat() / totalQuestions else 0f
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(300),
                label = "progress"
            )
            
            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Question Counter
            Text(
                text = "Question $currentQuestion of $totalQuestions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun QuizQuestionContent(
    question: QuizQuestion,
    selectedAnswer: Int?,
    onAnswerSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Question
            Text(
                text = "Question ${question.questionNumber}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = question.question,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Answer Options
            question.options.forEachIndexed { index, option ->
                val isSelected = selectedAnswer == index
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 4.dp else 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onAnswerSelected(index) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onAnswerSelected(index) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizNavigationFooter(
    currentQuestion: Int,
    totalQuestions: Int,
    hasAnswer: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Button
            Button(
                onClick = onPrevious,
                enabled = currentQuestion > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Previous")
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Next/Submit Button
            Button(
                onClick = if (currentQuestion < totalQuestions - 1) onNext else onSubmit,
                enabled = hasAnswer,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    if (currentQuestion < totalQuestions - 1) "Next" else "Submit Quiz"
                )
                if (currentQuestion < totalQuestions - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next",
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Submit",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizResultScreen(
    result: QuizResult,
    onRetake: () -> Unit,
    onBackToTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Result Icon
            Icon(
                imageVector = if (result.percentage >= 70f) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = "Quiz Result",
                modifier = Modifier.size(64.dp),
                tint = if (result.percentage >= 70f) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Score
            Text(
                text = "${result.score}/${result.maxScore}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Percentage
            Text(
                text = "${result.percentage.toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (result.percentage >= 70f) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Result Message
            Text(
                text = if (result.percentage >= 70f) {
                    "Great job! You passed the quiz!"
                } else {
                    "Keep studying! You can retake this quiz."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Retake Button
            Button(
                onClick = onRetake,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retake",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retake Quiz")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Back to Task Button
            OutlinedButton(
                onClick = onBackToTask,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back to Task")
            }
        }
    }
}
