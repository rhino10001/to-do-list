package rhino10001.todolist.model

import jakarta.persistence.*

@Entity
@Table(name = "tasks")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
open class TaskEntity(

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    open val project: ProjectEntity? = null,

    @Column(name = "title")
    open val title: String


) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TaskEntity

        if (id != other.id) return false
        if (project != other.project) return false
        if (title != other.title) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (project?.hashCode() ?: 0)
        result = 31 * result + title.hashCode()
        return result
    }
}
