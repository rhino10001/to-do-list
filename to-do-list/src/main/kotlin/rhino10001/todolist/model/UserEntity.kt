package rhino10001.todolist.model

import jakarta.persistence.*
import rhino10001.todolist.dto.UserDTO

@Entity
@Table(name = "users")
data class UserEntity(

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "username")
    val username: String,

    @Column(name = "password")
    val password: String,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "users_roles",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "role_id", referencedColumnName = "id")]
    )
    val roles: List<RoleEntity>
)

fun UserEntity.toDTO() = UserDTO(
    id = id ?: 0,
    username = username,
    password = password,
    roles = roles.map { it.toDTO() }
)