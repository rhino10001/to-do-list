package rhino10001.todolist.repository

import org.springframework.data.jpa.repository.JpaRepository
import rhino10001.todolist.model.TaskEntity

interface TaskRepository : JpaRepository<TaskEntity, Long> {

    fun findByProjectId(projectId: Long): List<TaskEntity>
}