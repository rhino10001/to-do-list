package rhino10001.todolist.repository

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.dao.EmptyResultDataAccessException
import rhino10001.todolist.model.ProjectEntity
import rhino10001.todolist.model.RoleEntity
import rhino10001.todolist.model.UserEntity

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProjectRepositoryTest @Autowired constructor(
    private val entityManager: TestEntityManager,
    private val projectRepository: ProjectRepository
) {

    @Test
    fun givenExistingUsername_whenFindByUserUsername_thenReturnsProjects() {

        val roleUser = entityManager.find(RoleEntity::class.java, 2)

        val user = UserEntity(
            username = "test_username",
            password = "test_encoded_password",
            roles = listOf(roleUser)
        )
        entityManager.persist(user)

        val first = ProjectEntity(
            user = user,
            title = "test_title",
            description = "test_description"
        )
        entityManager.persist(first)

        val second = ProjectEntity(
            user = user,
            title = "test_title",
            description = "test_description"
        )
        entityManager.persist(second)
        entityManager.flush()

//        given
        val username = "test_username"

//        when
        val found = projectRepository.findByUserUsername(username)

//        then
        assertEquals(2, found.size)
    }

    @Test
    fun givenUnknownUsername_whenFindByUserUsername_thenReturnsEmptyList() {

//        given
        val username = "test_unknown_username"

//        when
        val projects = projectRepository.findByUserUsername(username)

//        then
        assertTrue(projects.isEmpty())
    }

    @Test
    fun givenNewProject_whenSave_thenReturnsSaved() {

//        given
        val roleUser = entityManager.find(RoleEntity::class.java, 2)

        val user = UserEntity(
            username = "test_username",
            password = "test_encoded_password",
            roles = listOf(roleUser)
        )
        entityManager.persist(user)
        entityManager.flush()

        val foundUser = entityManager.find(UserEntity::class.java, user.id)
        val givenProjectEntity = ProjectEntity(
            user = foundUser,
            title = "test_title",
            description = "test_description"
        )

//        when
        val saved = projectRepository.save(givenProjectEntity)

//        then
        val expected = ProjectEntity(
            id = saved.id,
            user = foundUser,
            title = "test_title",
            description = "test_description"
        )

        assertNotNull(saved.id)
        assertEquals(expected, saved)
    }

    @Test
    fun givenExistingId_whenFindById_thenReturnsProject() {

        val roleUser = entityManager.find(RoleEntity::class.java, 2)

        val user = UserEntity(
            username = "test_username",
            password = "test_encoded_password",
            roles = listOf(roleUser)
        )
        entityManager.persist(user)

        val savedProject = ProjectEntity(
            user = user,
            title = "test_title",
            description = "test_description"
        )

        entityManager.persist(savedProject)
        entityManager.flush()

//        given
        val id = savedProject.id

//        when
        val found = id?.let { projectRepository.findById(it).get() }

//        then
        val expected = ProjectEntity(
            id = id,
            user = user,
            title = "test_title",
            description = "test_description"
        )

        assertNotNull(savedProject.id)
        assertEquals(expected, found)
    }

    @Test
    fun givenUnknownId_whenFindById_thenReturnsEmptyOptional() {

        val roleUser = entityManager.find(RoleEntity::class.java, 2)

        val user = UserEntity(
            username = "test_username",
            password = "test_encoded_password",
            roles = listOf(roleUser)
        )
        entityManager.persist(user)

        val savedProject = ProjectEntity(
            user = user,
            title = "test_title",
            description = "test_description"
        )

        entityManager.persist(savedProject)
        entityManager.flush()

//        given
        val id = savedProject.id?.plus(1)

//        when
        val found = id?.let { projectRepository.findById(it) }

//        then
        found?.let { assertTrue(it.isEmpty) }
    }

    @Test
    fun givenExistingId_whenDeleteById_thenSucceed() {

        val roleUser = entityManager.find(RoleEntity::class.java, 2)

        val user = UserEntity(
            username = "test_username",
            password = "test_encoded_password",
            roles = listOf(roleUser)
        )
        entityManager.persist(user)

        val savedProject = ProjectEntity(
            user = user,
            title = "test_title",
            description = "test_description"
        )

        entityManager.persist(savedProject)
        entityManager.flush()

//        given
        val id = savedProject.id

//        when
//        then
        assertDoesNotThrow { id?.let { projectRepository.deleteById(it) } }
        val found = id?.let { projectRepository.findById(it) }
        found?.let { assertTrue(it.isEmpty) }
    }

    @Test
    fun givenUnknownId_whenDeleteById_thenThrowsEmptyResultDataAccessException() {

        val roleUser = entityManager.find(RoleEntity::class.java, 2)

        val user = UserEntity(
            username = "test_username",
            password = "test_encoded_password",
            roles = listOf(roleUser)
        )
        entityManager.persist(user)

        val savedProject = ProjectEntity(
            user = user,
            title = "test_title",
            description = "test_description"
        )

        entityManager.persist(savedProject)
        entityManager.flush()

//        given
        val id = savedProject.id?.plus(1)

//        when
//        then
        assertThrows<EmptyResultDataAccessException> { id?.let { projectRepository.deleteById(it) } }
    }
}