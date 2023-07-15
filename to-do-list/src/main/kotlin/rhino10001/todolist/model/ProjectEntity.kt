package rhino10001.todolist.model

import jakarta.persistence.*

@Entity
@Table(name = "projects")
data class ProjectEntity(

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Column(name = "title")
    val title: String,

    @Column(name = "description")
    val description: String
)