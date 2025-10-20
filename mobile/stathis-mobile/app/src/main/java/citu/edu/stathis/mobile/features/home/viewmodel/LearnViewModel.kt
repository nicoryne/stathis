package citu.edu.stathis.mobile.features.home.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import citu.edu.stathis.mobile.core.streak.StreakManager

@HiltViewModel
class LearnViewModel @Inject constructor(
    val streakManager: StreakManager
) : ViewModel()


