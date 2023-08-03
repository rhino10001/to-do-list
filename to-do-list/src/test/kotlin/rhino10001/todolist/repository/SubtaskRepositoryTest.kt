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
import rhino10001.todolist.model.*

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SubtaskRepositoryTest @Autowired constructor(
    private val entityManager: TestEntityManager,
    private val subtaskRepository: SubtaskRepository
) {

    @Test
    fun givenExistingTaskId_whenFindByTaskId_thenReturnsSubtasks() {

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

        val task = TaskEntity(
            project = project,
            title = "test_title"
        )
        entityManager.persist(task)

        val first = SubtaskEntity(
            task = task,
            title = "test_title"
        )
        entityManager.persist(first)

        val second = SubtaskEntity(
            task = task,
            title = "test_title"
        )
        entityManager.persist(second)
        entityManager.flush()

//        given
        val id = task.id

//        when
        val found = id?.let { subtaskRepository.findByTaskId(it) }

//        then
        found?.let { Assertions.assertEquals(2, it.size) }
    }

    @Test
    fun givenTaskWithoutSubtasks_whenFindByTaskId_thenReturnsEmptyList() {

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

        val task = TaskEntity(
            project = project,
            title = "test_title"
        )
        entityManager.persist(task)
        entityManager.flush()

//        given
        val id = task.id

//        when
        val projects = id?.let { subtaskRepository.findByTaskId(it) }

//        then
        projects?.let { Assertions.assertTrue(it.isEmpty()) }
    }

    @Test
    fun givenNewSubtask_whenSave_thenReturnsSaved() {

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

        val task = TaskEntity(
            project = project,
            title = "test_title"
        )
        entityManager.persist(task)
        entityManager.flush()

        val givenTaskEntity = SubtaskEntity(
            task = task,
            title = "test_title"
        )

//        when
        val saved = subtaskRepository.save(givenTaskEntity)

//        then
        val expected = SubtaskEntity(
//            id = saved.id,
//            project = project,
            task = task,
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

        val task = TaskEntity(
            project = project,
            title = "test_title"
        )
        entityManager.persist(task)

        val savedSubtask = SubtaskEntity(
            task = task,
            title = "test_title"
        )
        entityManager.persist(savedSubtask)
        entityManager.flush()

//        given
        val id = savedSubtask.id

//        when
        val found = id?.let { subtaskRepository.findById(it).get() }

//        then
        val expected = SubtaskEntity(
            task = task,
            title = "test_title"
        )

        Assertions.assertNotNull(savedSubtask.id)
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

        val task = TaskEntity(
            project = project,
            title = "test_title"
        )
        entityManager.persist(task)

        val savedSubtask = SubtaskEntity(
            task = task,
            title = "test_title"
        )
        entityManager.persist(savedSubtask)
        entityManager.flush()

//        given
        val id = savedSubtask.id?.plus(1)

//        when
        val found = id?.let { subtaskRepository.findById(it) }

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

        val task = TaskEntity(
            project = project,
            title = "test_title"
        )
        entityManager.persist(task)

        val savedSubtask = SubtaskEntity(
            task = task,
            title = "test_title"
        )
        entityManager.persist(savedSubtask)
        entityManager.flush()

//        given
        val id = savedSubtask.id

//        when
//        then
        assertDoesNotThrow { id?.let { subtaskRepository.deleteById(it) } }
        val found = id?.let { subtaskRepository.findById(it) }
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

        val task = TaskEntity(
            project = project,
            title = "test_title"
        )
        entityManager.persist(task)

        val savedSubtask = SubtaskEntity(
            task = task,
            title = "test_title"
        )
        entityManager.persist(savedSubtask)
        entityManager.flush()

//        given
        val id = savedSubtask.id?.plus(1)

//        when
//        then
        assertThrows<EmptyResultDataAccessException> { id?.let { subtaskRepository.deleteById(it) } }
    }
}