package pe.com.smart.core.ui.message

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object UiMessageManager {

    private val _messages =
        MutableSharedFlow<UiMessage>(
            extraBufferCapacity = 10
        )

    val messages: SharedFlow<UiMessage> =
        _messages.asSharedFlow()

    fun success(
        title: String,
        message: String? = null
    ) {

        _messages.tryEmit(
            UiMessage.Success(
                title = title,
                message = message
            )
        )
    }

    fun error(
        title: String,
        message: String? = null
    ) {

        _messages.tryEmit(
            UiMessage.Error(
                title = title,
                message = message
            )
        )
    }

    fun info(
        title: String,
        message: String? = null
    ) {

        _messages.tryEmit(
            UiMessage.Info(
                title = title,
                message = message
            )
        )
    }

    fun warning(
        title: String,
        message: String? = null
    ) {

        _messages.tryEmit(
            UiMessage.Warning(
                title = title,
                message = message
            )
        )
    }
}