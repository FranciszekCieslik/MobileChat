package authentication

data class RegisterState(
    val email: String = "",
    val password: String = "",
    var isLoading: Boolean = false,
    val error: String? = null,
    val name: String = "",
    val photoUrl: String = "",
    val nickname: String = "",
    val bio: String = ""
)