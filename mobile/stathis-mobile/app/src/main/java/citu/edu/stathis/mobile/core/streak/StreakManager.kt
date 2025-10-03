package citu.edu.stathis.mobile.core.streak

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

@Singleton
class StreakManager @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    private val prefs = appContext.getSharedPreferences("streak_prefs", Context.MODE_PRIVATE)

    private val _streak: MutableStateFlow<Int> = MutableStateFlow(prefs.getInt(KEY_STREAK_COUNT, 0))
    val streak: StateFlow<Int> = _streak

    fun recordActivity(date: LocalDate = LocalDate.now()): Int {
        val lastDateStr = prefs.getString(KEY_LAST_ACTIVE_DATE, null)
        val lastDate = lastDateStr?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

        val newCount = when {
            lastDate == null -> 1
            lastDate.isEqual(date) -> _streak.value
            lastDate.plusDays(1).isEqual(date) -> (_streak.value.takeIf { it > 0 } ?: 0) + 1
            else -> 1
        }

        if (newCount != _streak.value || lastDate != date) {
            prefs.edit()
                .putInt(KEY_STREAK_COUNT, newCount)
                .putString(KEY_LAST_ACTIVE_DATE, date.toString())
                .apply()
            _streak.value = newCount
        }
        return newCount
    }

    fun resetIfGap(currentDate: LocalDate = LocalDate.now()) {
        val lastDateStr = prefs.getString(KEY_LAST_ACTIVE_DATE, null)
        val lastDate = lastDateStr?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (lastDate != null && lastDate.isBefore(currentDate.minusDays(1))) {
            prefs.edit()
                .putInt(KEY_STREAK_COUNT, 0)
                .apply()
            _streak.value = 0
        }
    }

    companion object {
        private const val KEY_STREAK_COUNT = "streak_count"
        private const val KEY_LAST_ACTIVE_DATE = "last_active_date"
    }
}


