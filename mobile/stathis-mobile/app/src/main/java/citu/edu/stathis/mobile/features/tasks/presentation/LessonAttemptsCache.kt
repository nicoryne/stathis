package citu.edu.stathis.mobile.features.tasks.presentation

import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory attempt counter for lesson tasks.
 * Backend does not persist attempts for lessons, so we maintain a session-level
 * counter to drive UI availability and attempt stats. We also sync with
 * progress.lessonCompleted by ensuring at least 1 attempt when completed.
 */
object LessonAttemptsCache {
    private val attemptsByTaskId: ConcurrentHashMap<String, Int> = ConcurrentHashMap()

    fun getAttempts(taskId: String): Int = attemptsByTaskId[taskId] ?: 0

    fun ensureAtLeast(taskId: String, minAttempts: Int) {
        val current = attemptsByTaskId[taskId] ?: 0
        if (minAttempts > current) attemptsByTaskId[taskId] = minAttempts
    }

    fun increment(taskId: String) {
        attemptsByTaskId[taskId] = getAttempts(taskId) + 1
    }
}





