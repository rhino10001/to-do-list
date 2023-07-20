package rhino10001.todolist.model

import jakarta.persistence.*

@Entity
@Table(name = "tasks")
data class TaskEntity(

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    val project: ProjectEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    val parent: TaskEntity? = null,

    @Column(name = "title")
    val title: String
)
