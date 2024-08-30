package com.example.coroutines_flows_jc

import android.app.Application
import com.example.coroutines_flows_jc.data.data_source.remote.MyApi
import com.example.coroutines_flows_jc.data.data_source.remote.StarWarsApi
import com.example.coroutines_flows_jc.data.repository.MyRepository
import com.example.coroutines_flows_jc.ui.signup.MyViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApp)
            modules(koinModule)
        }
    }
}

val koinModule = module {
    single {
        MyViewModel(get())
    }

    single {
        MyRepository(get(), get(), get())
    }

    single {
        Dispatchers.IO
    }

    single<MyApi> {
        Retrofit.Builder()
            .baseUrl("https://api.meeting-room.amalitech-dev.net/api/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(MyApi::class.java)
    }

    single<StarWarsApi> {
        Retrofit.Builder()
            .baseUrl("https://swapi.dev/api/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(StarWarsApi::class.java)
    }
}
