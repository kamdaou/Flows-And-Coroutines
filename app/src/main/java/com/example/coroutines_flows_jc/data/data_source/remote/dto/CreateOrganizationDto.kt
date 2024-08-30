package com.example.coroutines_flows_jc.data.data_source.remote.dto

import com.example.coroutines_flows_jc.domain.model.CreateOrganization

data class CreateOrganizationDto(
    val `data`: CreateOrganizationData? = null,
    val message: String? = null,
    val status: Boolean?,
) {
    fun toCreateOrganization(): CreateOrganization {
        return CreateOrganization(status ?: false)
    }
}
