package com.example.coroutines_flows_jc.data.data_source.remote.dto

import com.example.coroutines_flows_jc.domain.model.OrganizationType
import com.example.coroutines_flows_jc.domain.model.TypesOrganisation

data class OrganizationTypeDto(
    val typesOrganisation: List<TypesOrganisation>?
) {
    fun toOrganizationType(): OrganizationType {
        return OrganizationType(typesOrganisation ?: emptyList())
    }
}
