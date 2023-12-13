package com.example.coroutines_flows_jc.data.data_source.remote.dto

data class SwapiDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<SwapiResult>
)