package rhino10001.todolist.model

import jakarta.persistence.*
import rhino10001.todolist.dto.role.RoleDTO

@Entity
@Table(name = "roles")
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
        USER, ADMIN
    }
}

fun RoleEntity.toDTO() = RoleDTO(
    id = id ?: 0,
    type = type
)