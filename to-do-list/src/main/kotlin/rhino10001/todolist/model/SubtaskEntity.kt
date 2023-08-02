package rhino10001.todolist.model

import jakarta.persistence.*

@Entity
data class SubtaskEntity(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    val task: TaskEntity? = null,

    @Column(name = "title")
    override val title: String

) : TaskEntity(title = title)
