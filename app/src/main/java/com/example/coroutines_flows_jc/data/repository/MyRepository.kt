package com.example.coroutines_flows_jc.data.repository

import android.util.Log
import com.example.coroutines_flows_jc.data.data_source.remote.MyApi
import com.example.coroutines_flows_jc.data.data_source.remote.StarWarsApi
import com.example.coroutines_flows_jc.data.data_source.remote.dto.CreateOrganizationDto
import com.example.coroutines_flows_jc.data.data_source.remote.dto.LocationX
import com.example.coroutines_flows_jc.data.data_source.remote.dto.SwapiResult
import com.example.coroutines_flows_jc.data.data_source.util.ApiResult
import com.example.coroutines_flows_jc.data.data_source.util.UiText
import com.example.coroutines_flows_jc.domain.model.TypesOrganisation
import com.example.coroutines_flows_jc.domain.model.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONObject

@ExperimentalCoroutinesApi
class MyRepository(
    private val api: MyApi,
    private val starWarsApi: StarWarsApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun extractError(jsonObject: JSONObject): UiText? {
        if (jsonObject.has("errors")) {
            val errorsObject = jsonObject.getJSONObject("errors")
            val errorFields = errorsObject.keys()
            for (fields in errorFields) {
                val errorArray = errorsObject.getJSONArray(fields)
                val errors = mutableListOf<String>()
                for (i in 0 until errorArray.length()) {
                    errors.add(errorArray.getString(i))
                }
                if (errors.isNotEmpty())
                    return UiText.DynamicString(errors.first())
            }
        }
        if (jsonObject.has("error")) {
            return UiText.DynamicString(jsonObject.getString("error"))
        }
        if (jsonObject.has("message")) {
            return UiText.DynamicString(jsonObject.getString("message"))
        }
        return null
    }

    suspend fun fetchLocationsWithFlow(): Flow<List<LocationX>> = withContext(ioDispatcher) {
        flow {
            try {
                val apiResult = api.fetchLocations()
                if (apiResult.isSuccessful) {
                    apiResult.body()?.locations?.let { emit(it) }
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    suspend fun fetchEnterpriseTypesWithList(): ApiResult<List<TypesOrganisation>> =
        withContext(ioDispatcher) {
            try {
                val apiResult = api.fetchOrganizations()

                if (apiResult.isSuccessful) {
                    return@withContext ApiResult(apiResult.body()?.typesOrganisation)
                } else {
                    return@withContext ApiResult(
                        error = extractError(
                            JSONObject(
                                apiResult.errorBody().toString()
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                return@withContext ApiResult(error = e.message?.let { UiText.DynamicString(it) })
            }
        }

    suspend fun emitChannelFlow(): Flow<List<SwapiResult>> {
        Log.d("FlowSwapi", "Fetching page number \n")
        var i = 1
        return withContext(Dispatchers.IO) {
            channelFlow {
                var next: String?
                do {
                    Log.d("FlowSwapi", "Fetching page number $i\n")
                    val result = async { starWarsApi.getPeople(i++) }
                    result.await().let {
                        if (it.isSuccessful) {
                            val body = result.await().body()
                            next = body?.next
                            send(body?.results ?: emptyList())
                        } else {
                            throw Exception("An exception occurred")
                        }
                    }
                } while (next != null)
            }
        }
    }

    suspend fun emitFlow(): Flow<List<SwapiResult>> {
        return withContext(ioDispatcher) {
            var i = 1
            flow {
                var next: String? = ""
                do {
                    Log.d("FlowSwapi", "Fetching page number flow $i\n")
                    val result =/* async {*/ starWarsApi.getPeople(i++) /*}*/
                    result/*.await()*/.let { response ->
                        if (response.isSuccessful) {
                            val body = response.body()
                            next = body?.next
                            emit(body?.results ?: emptyList())
                        } else {
                            extractError(
                                JSONObject(
                                    response.errorBody().toString()
                                )
                            )
                        }
                    }
                } while (next != null)
            }
        }
    }
}
