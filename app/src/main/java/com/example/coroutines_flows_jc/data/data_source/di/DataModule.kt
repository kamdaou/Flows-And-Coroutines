//package com.example.coroutines_flows_jc.data.data_source.di
//
//import com.example.coroutines_flows_jc.data.data_source.remote.MyApi
//import com.example.coroutines_flows_jc.data.repository.MyRepository
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.components.ViewModelComponent
//import dagger.hilt.android.scopes.ViewModelScoped
//import dagger.hilt.components.SingletonComponent
//import kotlinx.coroutines.CoroutineDispatcher
//import retrofit2.Retrofit
//import retrofit2.converter.moshi.MoshiConverterFactory
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//object DataModule {
//    @Singleton
//    @Provides
//    fun provideApi(): MyApi {
//        return Retrofit.Builder()
//            .baseUrl("https://api.meeting-room.amalitech-dev.net/api")
//            .addConverterFactory(MoshiConverterFactory.create())
//            .build()
//            .create(MyApi::class.java)
//    }
//
//    @Singleton
//    @Provides
//    fun provideRepos(
//        api: MyApi,
//    ): MyRepository {
//        return MyRepository(api)
//    }
//}