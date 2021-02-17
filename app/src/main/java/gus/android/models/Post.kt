package gus.android.models

data class Post(
    var description: String = "",
    var imageUrl: String = "",
    var creationTimeMs: Long = 0,
    var user: User? = null
)