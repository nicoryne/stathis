package citu.edu.stathis.mobile.features.auth.data.enums

enum class UserRoles {
    GUEST_USER,
    STUDENT,
    TEACHER;

    fun withoutPrefix(): String {
        return when (this) {
            GUEST_USER -> "GUEST_USER"
            STUDENT -> "STUDENT"
            TEACHER -> "TEACHER"
        }
    }

    fun toSpringSecurityRole(): String {
        return when (this) {
            GUEST_USER -> "ROLE_GUEST_USER"
            STUDENT -> "ROLE_STUDENT"
            TEACHER -> "ROLE_TEACHER"
        }
    }
}