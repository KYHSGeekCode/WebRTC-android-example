package com.skfo763.rtcandroid_example.utils

class TokenManager {

    companion object {
        const val DEFAULT_PASSWORD = "123456"

        @JvmStatic
        fun getToken(isTeacher: Boolean): String {
            return if (isTeacher) {
                "teacher"
            } else {
                "student"
            }
        }
    }
}