package com.example.coroutines_flows_jc.data.data_source.remote.dto

import android.service.autofill.UserData

data class LoginDto(
    val `data`: UserData?,
    val status: Boolean?,
    val successLoginMessage: String?,
    val token: String?
)