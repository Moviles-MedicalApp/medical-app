package pe.com.smart.core.ui.message

sealed class UiMessage(
    open val title: String,
    open val message: String? = null
) {

    data class Success(
        override val title: String,
        override val message: String? = null
    ) : UiMessage(title, message)

    data class Error(
        override val title: String,
        override val message: String? = null
    ) : UiMessage(title, message)

    data class Info(
        override val title: String,
        override val message: String? = null
    ) : UiMessage(title, message)

    data class Warning(
        override val title: String,
        override val message: String? = null
    ) : UiMessage(title, message)
}