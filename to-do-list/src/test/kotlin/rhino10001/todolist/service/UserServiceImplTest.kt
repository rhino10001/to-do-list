package rhino10001.todolist.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.security.crypto.password.PasswordEncoder
import rhino10001.todolist.dto.RoleDTO
import rhino10001.todolist.dto.UserDTO
import rhino10001.todolist.dto.toEntity
import rhino10001.todolist.model.RoleEntity
import rhino10001.todolist.model.UserEntity
import rhino10001.todolist.model.toDTO
import rhino10001.todolist.repository.RoleRepository
import rhino10001.todolist.repository.UserRepository
import rhino10001.todolist.service.impl.UserServiceImpl

@ExtendWith(MockitoExtension::class)
class UserServiceImplTest(

    @Mock
    private val userRepository: UserRepository,

    @Mock
    private val roleRepository: RoleRepository,

    @Mock
    private val passwordEncoder: PasswordEncoder
) {

    @InjectMocks
    private lateinit var userService: UserServiceImpl

    @Test
    fun givenNewUser_whenRegister_thenReturnsSameUserWithIdAndEncodedPasswordAndRoleUser() {

//        given
        val givenUserDTO = UserDTO(
            username = "test_unique_username",
            password = "test_password"
        )

//        when
        val roleUserEntity = RoleEntity(
            id = 1,
            type = RoleEntity.Type.ROLE_USER
        )
        `when`(roleRepository.findByType(RoleEntity.Type.ROLE_USER)).thenReturn(roleUserEntity)

        val encodedPassword = "test_encoded_password"
        `when`(passwordEncoder.encode(givenUserDTO.password)).thenReturn(encodedPassword)

        val toSaveEntity = UserEntity(
            username = givenUserDTO.username,
            password = encodedPassword,
            roles = listOf(roleUserEntity)
        )
        val savedEntity = UserEntity(
            id = 1,
            username = givenUserDTO.username,
            password = encodedPassword,
            roles = listOf(roleUserEntity)
        )
        `when`(userRepository.save(toSaveEntity)).thenReturn(savedEntity)

//        then
        val expected = UserDTO(
            id = 1,
            username = givenUserDTO.username,
            password = encodedPassword,
            roles = listOf(
                RoleDTO(
                    id = 1,
                    type = RoleEntity.Type.ROLE_USER
                )
            )
        )

        assertEquals(expected, userService.register(givenUserDTO))
    }

    @Test
    fun givenUserWithExistingUsername_whenRegister_thenThrowsException() {

//        given
        val givenUserDTO = UserDTO(
            username = "test_not_unique_username",
            password = "test_password"
        )

//        when
        val roleUserEntity = RoleEntity(
            id = 1,
            type = RoleEntity.Type.ROLE_USER
        )
        `when`(roleRepository.findByType(RoleEntity.Type.ROLE_USER)).thenReturn(roleUserEntity)

        val encodedPassword = "test_encoded_password"
        `when`(passwordEncoder.encode(givenUserDTO.password)).thenReturn(encodedPassword)

        val toSave = givenUserDTO.copy(
            roles = listOf(roleUserEntity.toDTO()),
            password = encodedPassword
        )

        val exception = DataIntegrityViolationException("Indifferently")
        `when`(userRepository.save(toSave.toEntity())).thenThrow(exception)

//        then
        assertThrows<DataIntegrityViolationException> { userService.register(givenUserDTO) }
    }

    @Test
    fun givenExistingUsername_whenFindByUserName_thenReturnsFoundUser() {

//        given
        val givenUsername = "test_registered_username"

//        when
        val foundUserEntity = UserEntity(
            id = 1,
            username = givenUsername,
            password = "test_encoded_password",
            roles = listOf(
                RoleEntity(
                    id = 1,
                    type = RoleEntity.Type.ROLE_USER
                )
            )
        )
        `when`(userRepository.findByUsername(givenUsername)).thenReturn(foundUserEntity)

//        then
        val expected = UserDTO(
            id = 1,
            username = givenUsername,
            password = "test_encoded_password",
            roles = listOf(
                RoleDTO(
                    id = 1,
                    type = RoleEntity.Type.ROLE_USER,
                )
            )
        )

        assertEquals(expected, userService.findByUsername(givenUsername))
    }

    @Test
    fun givenUnknownUser_whenFindByUsername_thenThrowsException() {

//        given
        val givenUsername = "test_unknown_username"

//        when
        val exception = EmptyResultDataAccessException("Result must not be null", 1)
        `when`(userRepository.findByUsername(givenUsername)).thenThrow(exception)

//        then
        assertThrows<EmptyResultDataAccessException> { userService.findByUsername(givenUsername) }
    }
}