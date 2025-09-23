package citu.edu.stathis.mobile.features.auth.ui.utils

import android.util.Patterns

object EmailValidator {

    fun isValidEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) {
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false
        }

         val stricterEmailRegex = Regex(
             "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
             "\\@" +
             "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
             "(" +
             "\\." +
             "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
             ")+"
         )

         if (!stricterEmailRegex.matches(email)) {
             return false
         }

        if (email.startsWith(".") || email.endsWith(".")) {
            return false
        }


        if (email.contains("..")) {
            return false
        }
        val atIndex = email.indexOf('@')

        if (atIndex != -1) {
            val domainPart = email.substring(atIndex + 1)
            if (domainPart.startsWith(".") || domainPart.endsWith(".")) {
                return false
            }
            if (!domainPart.contains(".")) {
                return false
            }
        }

        return true
    }
}