package citu.edu.stathis.mobile.features.tasks.data.model

data class LessonPage(
    val id: String,
    val pageNumber: Int,
    val subtitle: String,
    val paragraph: String
)

data class LessonTemplate(
    val physicalId: String,
    val title: String,
    val description: String,
    val pages: List<LessonPage>
)

