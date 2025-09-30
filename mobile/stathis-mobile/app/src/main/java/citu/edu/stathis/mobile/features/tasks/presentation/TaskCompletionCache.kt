package citu.edu.stathis.mobile.features.tasks.presentation

import java.util.concurrent.ConcurrentHashMap

/**
 * Ephemeral, in-memory cache to reflect task completions immediately in UI
 * when backend progress endpoints are not accessible from list context.
 */
object TaskCompletionCache {
    private val completedTaskIds: MutableSet<String> = ConcurrentHashMap.newKeySet()

    fun markCompleted(taskId: String) {
        completedTaskIds.add(taskId)
    }

    fun isCompleted(taskId: String): Boolean = completedTaskIds.contains(taskId)

    fun clear() {
        completedTaskIds.clear()
    }
}


