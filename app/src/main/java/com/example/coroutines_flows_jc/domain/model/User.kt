package com.example.coroutines_flows_jc.domain.model

data class User(
    val username: String,
    val organizationName: String,
    val email: String,
    val typeOfOrganization: Int,
    val location: Int,
    val password: String,
    val passwordConfirmation: String,
)
