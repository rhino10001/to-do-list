package rhino10001.todolist.repository

import org.springframework.data.jpa.repository.JpaRepository
import rhino10001.todolist.model.SubtaskEntity

interface SubtaskRepository: JpaRepository<SubtaskEntity, Long> {

    fun findByTaskId(taskId: Long): List<SubtaskEntity>
}