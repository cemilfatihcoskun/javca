package com.sstek.javca.data.source

class FakeAuthDataSource {
    suspend fun login(email: String, password: String): String? {
        if (email == "harezmi@gmail.com" && password == "harezmi1234") {
            return "harezmi"
        }
        return null
    }
}