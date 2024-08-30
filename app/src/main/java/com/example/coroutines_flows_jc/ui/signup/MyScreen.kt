package com.example.coroutines_flows_jc.ui.signup

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.compose.koinViewModel

@ExperimentalCoroutinesApi
@Composable
fun MyScreen(
    viewModel: MyViewModel = koinViewModel(),
    showSnackbar: suspend (String) -> Unit
) {


    val locations by viewModel.locationsSharedPlusFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val types by viewModel.typesSharedPlusList.collectAsStateWithLifecycle(initialValue = emptyList())
    val channel by viewModel.peopleChannelFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val flow by viewModel.peopleFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val error by viewModel.error.collectAsStateWithLifecycle(initialValue = null)
    val context = LocalContext.current
    val loadingType by viewModel.loadingType.collectAsStateWithLifecycle()
    val loadingLocation by viewModel.loadingLocation.collectAsStateWithLifecycle()
    val loadingSwapiFlow by viewModel.loadingSwapiFlow.collectAsStateWithLifecycle()
    val loadingSwapiChannel by viewModel.loadingSwapiChannel.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = error) {
        error?.asString(context)?.let { showSnackbar(it) }
    }

    Column(
        Modifier
            .fillMaxSize()
        /*.padding(paddingValues)*/
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .border(width = 1.dp, color = Color.Cyan)
        ) {
            LazyColumn(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .border(width = 1.dp, color = Color.Cyan),
                horizontalAlignment = Alignment.Start
            ) {
                item {
                    Text(text = "Locations", style = MaterialTheme.typography.titleLarge)
                }
                if (loadingLocation) {
                    item {
                        CircularProgressIndicator()
                    }
                } else {
                    items(locations) {
                        Text(text = it.name)
                    }
                }
            }
            LazyColumn(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f),
                horizontalAlignment = Alignment.End
            ) {
                item {
                    Text(text = "Types", style = MaterialTheme.typography.titleLarge)
                }
                if (loadingType) {
                    item {
                        CircularProgressIndicator()
                    }
                } else {
                    items(types) {
                        Text(text = it.name)
                    }
                }
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .border(width = 1.dp, color = Color.Cyan)
        ) {
            LazyColumn(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(width = 1.dp, color = Color.Cyan),
                horizontalAlignment = Alignment.Start
            ) {
                item {
                    Text(text = "Flow", style = MaterialTheme.typography.titleLarge)
                }
                items(flow) {
                    Text(text = it.name)
                }
                if (loadingSwapiFlow) {
                    item {
                        CircularProgressIndicator()
                    }
                }
            }
            LazyColumn(
                Modifier
                    .weight(1f)
                    .border(width = 1.dp, color = Color.Cyan)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                item {
                    Text(text = "Channel", style = MaterialTheme.typography.titleLarge)
                }
                items(channel) {
                    Text(text = it.name)
                }
                if (loadingSwapiChannel) {
                    item {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
