import java.io.Serializable

data class Student(
    val id: Int,
    var nom: String,
    var prenom: String,
    var ville: String,
    var sexe: String,
    var filiere: String,
    var image: String
) : Serializable
