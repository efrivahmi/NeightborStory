package com.efrivahmi.neighborstory.data.response

data class Login(
    val loginResult: LoginResult,
    val error: Boolean,
    val message: String
)
data class LoginResult(
    val name: String,
    val token: String,
    val userId: String
)
