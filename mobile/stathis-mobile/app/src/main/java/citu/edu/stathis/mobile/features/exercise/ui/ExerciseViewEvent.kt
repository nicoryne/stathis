package citu.edu.stathis.mobile.features.exercise.ui

/**
 * Events emitted by the ExerciseViewModel.
 * These events are one-time events that should be consumed by the UI.
 */
sealed class ExerciseViewEvent {
    /**
     * Event to show a snackbar message
     */
    data class ShowSnackbar(val message: String) : ExerciseViewEvent()
    
    /**
     * Event to navigate back to exercise selection
     */
    object NavigateToExerciseSelection : ExerciseViewEvent()
    
    /**
     * Event to show a warning about low accuracy
     */
    data class ShowLowAccuracyWarning(val message: String) : ExerciseViewEvent()
}
