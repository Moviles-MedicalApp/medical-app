package pe.com.smart.data.local

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SessionManager {

    private val _sessionExpired =
        MutableSharedFlow<Unit>(
            extraBufferCapacity = 1
        )

    val sessionExpired: SharedFlow<Unit> =
        _sessionExpired.asSharedFlow()

    fun notifySessionExpired() {

        TokenStorage.clearToken()

        _sessionExpired.tryEmit(Unit)
    }
}