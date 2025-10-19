package citu.edu.stathis.mobile.features.tasks.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Ephemeral, in-memory cache to reflect task completions immediately in UI
 * when backend progress endpoints are not accessible from list context.
 */
object TaskCompletionCache {
    private val completedTaskIds: MutableSet<String> = ConcurrentHashMap.newKeySet()
    private val _completionUpdates = MutableStateFlow(0L)
    val completionUpdates: StateFlow<Long> = _completionUpdates.asStateFlow()

    fun markCompleted(taskId: String) {
        completedTaskIds.add(taskId)
        _completionUpdates.value = System.currentTimeMillis()
    }

    fun isCompleted(taskId: String): Boolean = completedTaskIds.contains(taskId)

    fun clear() {
        completedTaskIds.clear()
        _completionUpdates.value = System.currentTimeMillis()
    }
}


