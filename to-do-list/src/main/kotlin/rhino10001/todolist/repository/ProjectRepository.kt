package rhino10001.todolist.repository

import org.springframework.data.jpa.repository.JpaRepository
import rhino10001.todolist.model.ProjectEntity

interface ProjectRepository: JpaRepository<ProjectEntity, Long> {
}