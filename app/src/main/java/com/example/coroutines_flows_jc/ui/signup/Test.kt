package com.example.coroutines_flows_jc.ui.signup

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.shareIn

class MessagesService(
    messagesSource: Flow<Int>,
    scope: CoroutineScope
) {
    private val source = messagesSource
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed()
        )
    fun observeMessages(fromUserId: String) = source
        .filter { it % 2 == 0 }
}
// Can start multiple connections to the source
class MessagesServices(
    val messagesSource: Flow<Int>,
) {
    fun observeMessages(fromUserId: String) = messagesSource
        .filter { it % 2 == 0 }
}
