package rhino10001.todolist.model

import jakarta.persistence.*
import rhino10001.todolist.dto.UserDTO

@Entity
@Table(name = "\"user\"")
data class UserEntity(

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "username", unique = true)
    val username: String,

    @Column(name = "password")
    var password: String,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_role",
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