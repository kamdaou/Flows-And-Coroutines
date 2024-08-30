package com.example.coroutines_flows_jc.data.data_source.remote

import com.example.coroutines_flows_jc.data.data_source.remote.dto.CreateOrganizationDto
import com.example.coroutines_flows_jc.data.data_source.remote.dto.LocationDto
import com.example.coroutines_flows_jc.data.data_source.remote.dto.OrganizationTypeDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MyApi {
    @POST("OrganisationUser/create")
    suspend fun createOrganization(
        @Query("username") username: String,
        @Query("organization_name") organizationName: String,
        @Query("email") email: String,
        @Query("password") password: String,
        @Query("confirm_password") confirmPassword: String,
        @Query("location_id") locationId: String,
        @Query("type_organisation_id") typeOrganisationId: String,
    ): Response<CreateOrganizationDto>

    @GET("locations")
    suspend fun fetchLocations(): Response<LocationDto>

    @GET("typesOrganisation")
    suspend fun fetchOrganizations(): Response<OrganizationTypeDto>
}
