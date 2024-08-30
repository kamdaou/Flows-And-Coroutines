package com.example.coroutines_flows_jc.data.data_source.util

data class ApiResult<T>(
    val data: T? = null,
    val error: UiText? = null
)