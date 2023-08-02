package rhino10001.todolist.model

import jakarta.persistence.*
import rhino10001.todolist.dto.RoleDTO

@Entity
@Table(name = "role")
data class RoleEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    val type: Type,

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    val users: List<UserEntity> = listOf()
) {

    enum class Type {
        ROLE_USER, ROLE_ADMIN
    }
}

fun RoleEntity.toDTO() = RoleDTO(
    id = id ?: 0,
    type = type
)