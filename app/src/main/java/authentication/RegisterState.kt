package authentication

data class RegisterState(
    val email: String = "",
    val password: String = "",
    var isLoading: Boolean = false,
    val error: String? = null,
)

data class User(
    val email: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val bio: String = "",
    val friends: List<String>,
    val sentInvites: List<String>,
)