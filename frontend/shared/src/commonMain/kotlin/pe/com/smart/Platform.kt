package pe.com.smart

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform