package rhino10001.todolist.repository

import org.junit.jupiter.api.Assertions
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
import rhino10001.todolist.model.TaskEntity
import rhino10001.todolist.model.UserEntity

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryTest @Autowired constructor(
    private val entityManager: TestEntityManager,
    private val taskRepository: TaskRepository
) {

    @Test
    fun givenExistingProjectId_whenFindByProjectId_thenReturnsTasks() {

        val roleUser = entityManager.find(RoleEntity::class.java, 2)

        val user = UserEntity(
            username = "test_username",
            password = "test_encoded_password",
            roles = listOf(roleUser)
        )
        entityManager.persist(user)

        val project = ProjectEntity(
            user = user,
            title = "test_title",
            description = "test_description"
        )
        entityManager.persist(project)

        val first = TaskEntity(
            project = project,
            title = "test_title"
        )
        entityManager.persist(first)

        val second = TaskEntity(
            project = project,
            title = "test_title"
        )
        entityManager.persist(second)
        entityManager.flush()

//        given
        val id = project.id

//        when
        val found = id?.let { taskRepository.findByProjectId(it) }

//        then
        found?.let { Assertions.assertEquals(2, it.size) }
    }

    @Test
    fun givenProjectWithoutTasks_whenFindByProjectId_thenReturnsEmptyList() {

        val roleUser = entityManager.find(RoleEntity::class.java, 2)

        val user = UserEntity(
            username = "test_username",
            password = "test_encoded_password",
            roles = listOf(roleUser)
        )
        entityManager.persist(user)

        val project = ProjectEntity(
            user = user,
            title = "test_title",
            description = "test_description"
        )
        entityManager.persist(project)
        entityManager.flush()

//        given
        val id = project.id

//        when
        val projects = id?.let { taskRepository.findByProjectId(it) }

//        then
        projects?.let { Assertions.assertTrue(it.isEmpty()) }
    }

    @Test
    fun givenNewTask_whenSave_thenReturnsSaved() {

//        given
        val roleUser = entityManager.find(RoleEntity::class.java, 2)

        val user = UserEntity(
            username = "test_username",
            password = "test_encoded_password",
            roles = listOf(roleUser)
        )
        entityManager.persist(user)

        val project = ProjectEntity(
            user = user,
            title = "test_title",
            description = "test_description"
        )
        entityManager.persist(project)
        entityManager.flush()

        val givenTaskEntity = TaskEntity(
            project = project,
            title = "test_title"
        )

//        when
        val saved = taskRepository.save(givenTaskEntity)

//        then
        val expected = TaskEntity(
            id = saved.id,
            project = project,
            title = "test_title",
        )

        Assertions.assertNotNull(saved.id)
        Assertions.assertEquals(expected, saved)
    }

    @Test
    fun givenExistingId_whenFindById_thenReturnsTask() {

        val roleUser = entityManager.find(RoleEntity::class.java, 2)

        val user = UserEntity(
            username = "test_username",
            password = "test_encoded_password",
            roles = listOf(roleUser)
        )
        entityManager.persist(user)

        val project = ProjectEntity(
            user = user,
            title = "test_title",
            description = "test_description"
        )
        entityManager.persist(project)

        val savedTask = TaskEntity(
            project = project,
            title = "test_title"
        )
        entityManager.persist(savedTask)
        entityManager.flush()

//        given
        val id = savedTask.id

//        when
        val found = id?.let { taskRepository.findById(it).get() }

//        then
        val expected = TaskEntity(
            id = id,
            project = project,
            title = "test_title"
        )

        Assertions.assertNotNull(savedTask.id)
        Assertions.assertEquals(expected, found)
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

        val project = ProjectEntity(
            user = user,
            title = "test_title",
            description = "test_description"
        )
        entityManager.persist(project)

        val savedTask = TaskEntity(
            project = project,
            title = "test_title"
        )
        entityManager.persist(savedTask)
        entityManager.flush()

//        given
        val id = savedTask.id?.plus(1)

//        when
        val found = id?.let { taskRepository.findById(it) }

//        then
        found?.let { Assertions.assertTrue(it.isEmpty) }
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

        val project = ProjectEntity(
            user = user,
            title = "test_title",
            description = "test_description"
        )
        entityManager.persist(project)

        val savedTask = TaskEntity(
            project = project,
            title = "test_title"
        )
        entityManager.persist(savedTask)
        entityManager.flush()

//        given
        val id = savedTask.id

//        when
//        then
        assertDoesNotThrow { id?.let { taskRepository.deleteById(it) } }
        val found = id?.let { taskRepository.findById(it) }
        found?.let { Assertions.assertTrue(it.isEmpty) }
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

        val project = ProjectEntity(
            user = user,
            title = "test_title",
            description = "test_description"
        )
        entityManager.persist(project)

        val savedTask = TaskEntity(
            project = project,
            title = "test_title"
        )
        entityManager.persist(savedTask)
        entityManager.flush()

//        given
        val id = savedTask.id?.plus(1)

//        when
//        then
        assertThrows<EmptyResultDataAccessException> { id?.let { taskRepository.deleteById(it) } }
    }
}