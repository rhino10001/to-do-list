package rhino10001.todolist.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.EmptyResultDataAccessException
import rhino10001.todolist.model.RoleEntity
import rhino10001.todolist.model.UserEntity


@DataJpaTest(showSql = false)
class UserRepositoryTest @Autowired constructor(
    private val entityManager: TestEntityManager,
    private val userRepository: UserRepository,
) {

    @Test
    fun givenNewUser_whenSave_thenReturnsSaved() {

//        given
        val roleUser = entityManager.find(RoleEntity::class.java, 2)
        val givenUserEntity = UserEntity(
            username = "test_unique_username",
            password = "test_encoded_password",
            roles = listOf(roleUser)
        )

//        when
        val saved = userRepository.save(givenUserEntity)

//        then
        val expected = UserEntity(
            id = saved.id,
            username = "test_unique_username",
            password = "test_encoded_password",
            roles = listOf(roleUser)
        )

        assertEquals("ROLE_USER", roleUser.type.name)
        assertNotNull(saved.id)
        assertEquals(expected, saved)
    }

    @Test
    fun givenUserWithExistingUsername_whenSave_thenThrowsDataIntegrityViolationException() {

        val roleUser = entityManager.find(RoleEntity::class.java, 2)
        val uniqueUsernameEntity = UserEntity(
            username = "test_unique_username",
            password = "test_encoded_password",
            roles = listOf(roleUser)
        )
        entityManager.persist(uniqueUsernameEntity)
        entityManager.flush()

//        given
        val givenUserEntity = UserEntity(
            username = "test_unique_username",
            password = "test_encoded_password",
            roles = listOf(roleUser)
        )

//        when
        assertEquals("ROLE_USER", roleUser.type.name)
        assertThrows<DataIntegrityViolationException> { userRepository.save(givenUserEntity) }
    }

    @Test
    fun givenExistingUsername_whenFindByUsername_thenReturnUser() {

        val roleUser = entityManager.find(RoleEntity::class.java, 2)
        val registeredUserEntity = UserEntity(
            username = "test_username",
            password = "test_encoded_password",
            roles = listOf(roleUser)
        )
        entityManager.persist(registeredUserEntity)
        entityManager.flush()

//        given
        val givenUsername = registeredUserEntity.username

//        when
        val foundUser = userRepository.findByUsername(givenUsername)

//        then
        val expected = UserEntity(
            id = registeredUserEntity.id,
            username = "test_username",
            password = "test_encoded_password",
            roles = listOf(roleUser)
        )
        
        assertEquals("ROLE_USER", roleUser.type.name)
        assertNotNull(registeredUserEntity.id)
        assertEquals(expected, foundUser)
    }

    @Test
    fun givenUnknownUsername_whenFindByUsername_thenThrowsEmptyResultDataAccessException() {

        val roleUser = entityManager.find(RoleEntity::class.java, 2)
        val registeredUserEntity = UserEntity(
            username = "test_username",
            password = "test_encoded_password",
            roles = listOf(roleUser)
        )
        entityManager.persist(registeredUserEntity)
        entityManager.flush()

//        given
        val givenUsername = "est_username"

//        when
        assertEquals("ROLE_USER", roleUser.type.name)
        assertThrows<EmptyResultDataAccessException> { userRepository.findByUsername(givenUsername) }
    }
}