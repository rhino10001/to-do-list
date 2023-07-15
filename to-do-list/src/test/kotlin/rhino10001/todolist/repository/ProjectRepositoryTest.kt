package rhino10001.todolist.repository

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class ProjectRepositoryTest @Autowired constructor(
    private val projectRepository: ProjectRepository
) {

    @Test
    fun givenExistingUsername_whenFindByUserUsername_thenReturnsProjects() {

    }

    @Test
    fun givenUnknownUsername_whenFindByUserUsername_thenThrowsEmptyResultDataAccessException() {

    }

    @Test
    fun givenNewProject_whenSave_thenReturnsSaved() {

    }

    @Test
    fun givenExistingId_whenFindById_thenReturnsProject() {

    }

    @Test
    fun givenUnknownId_whenFindById_thenThrowsEmptyResultDataAccessException() {

    }

    @Test
    fun givenExistingId_whenDeleteById_thenSucceed() {

    }

    @Test
    fun givenUnknownId_whenDeleteById_then_____________() {

    }
}