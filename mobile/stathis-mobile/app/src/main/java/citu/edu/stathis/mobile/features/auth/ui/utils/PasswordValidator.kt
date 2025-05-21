package citu.edu.stathis.mobile.features.auth.ui.utils

import citu.edu.stathis.mobile.features.auth.ui.components.PasswordStrength

object PasswordValidator {
    fun String.hasMinLength(minLength: Int): Boolean {
        return this.length >= minLength
    }

    fun String.hasUpperCase(): Boolean {
        return this.any { it.isUpperCase() }
    }

    fun String.hasLowerCase(): Boolean {
        return this.any { it.isLowerCase() }
    }

    fun String.hasNumber(): Boolean {
        return this.any { it.isDigit() }
    }

    fun String.hasSpecialChar(): Boolean {
        val specialChars = "!@#$%^&*()_-+=<>?/[]{},.:;|\\~`"
        return this.any { specialChars.contains(it) }
    }

    fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength.EMPTY

        var score = 0

        if (password.hasMinLength(8)) score++
        if (password.hasUpperCase()) score++
        if (password.hasLowerCase()) score++
        if (password.hasNumber()) score++
        if (password.hasSpecialChar()) score++

        return when {
            score <= 2 -> PasswordStrength.WEAK
            score <= 4 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }

    fun isValidPassword(password: String): Boolean {
        return password.hasMinLength(8) &&
                password.hasUpperCase() &&
                password.hasLowerCase() &&
                password.hasNumber() &&
                password.hasSpecialChar()
    }

    fun doPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }
}