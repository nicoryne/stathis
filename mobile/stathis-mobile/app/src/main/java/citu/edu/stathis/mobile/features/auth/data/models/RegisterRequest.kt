package citu.edu.stathis.mobile.features.auth.data.models

import citu.edu.stathis.mobile.features.auth.data.enums.UserRoles

data class RegisterRequest (
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val userRole: UserRoles
)