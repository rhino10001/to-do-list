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
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import rhino10001.todolist.dto.RoleDTO
import rhino10001.todolist.dto.UserDTO
import rhino10001.todolist.dto.request.LoginRequest
import rhino10001.todolist.dto.request.RefreshRequest
import rhino10001.todolist.dto.response.ChangePasswordResponse
import rhino10001.todolist.dto.response.LoginResponse
import rhino10001.todolist.dto.response.RefreshResponse
import rhino10001.todolist.dto.toEntity
import rhino10001.todolist.exception.ChangePasswordException
import rhino10001.todolist.exception.JwtAuthenticationException
import rhino10001.todolist.model.RoleEntity
import rhino10001.todolist.model.UserEntity
import rhino10001.todolist.model.toDTO
import rhino10001.todolist.repository.RoleRepository
import rhino10001.todolist.repository.UserRepository
import rhino10001.todolist.security.JwtTokenProvider
import rhino10001.todolist.service.impl.UserServiceImpl

@ExtendWith(MockitoExtension::class)
class UserServiceImplTest(

    @Mock
    private val userRepository: UserRepository,

    @Mock
    private val roleRepository: RoleRepository,

    @Mock
    private val passwordEncoder: PasswordEncoder,

    @Mock
    private val authenticationManager: AuthenticationManager,

    @Mock
    private val jwtTokenProvider: JwtTokenProvider
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
    fun givenUserWithCorrectData_whenLogin_thenReturnsUsernameAndTokens() {

//        given
        val userRequest = LoginRequest(
            username = "test_username",
            password = "test_password"
        )

//        when
        val foundUser = UserDTO(
            username = userRequest.username,
            roles = listOf(RoleDTO(type = RoleEntity.Type.ROLE_USER))
        )
        `when`(userRepository.findByUsername(userRequest.username)).thenReturn(foundUser.toEntity())

        val accessToken = "test_access_token"
        `when`(jwtTokenProvider.generateAccessToken(foundUser.username, foundUser.roles)).thenReturn(accessToken)

        val refreshToken = "test_refresh_token"
        `when`(jwtTokenProvider.generateRefreshToken(foundUser.username)).thenReturn(refreshToken)

//        then
        val expectedResponse = LoginResponse(
            username = userRequest.username,
            accessToken = accessToken,
            refreshToken = refreshToken
        )

        assertEquals(expectedResponse, userService.login(userRequest.username, userRequest.password))
    }

    @Test
    fun givenUserWithUnknownUsername_whenLogin_thenThrowsException() {

//        given
        val userRequest = LoginRequest(
            username = "test_unknown_username",
            password = "test_password"
        )

//        when
        val authentication = UsernamePasswordAuthenticationToken(userRequest.username, userRequest.password)
        val exception = InternalAuthenticationServiceException("Indifferently")
        `when`(authenticationManager.authenticate(authentication)).thenThrow(exception)

//        then
        assertThrows<InternalAuthenticationServiceException> {
            userService.login(
                userRequest.username,
                userRequest.password
            )
        }
    }

    @Test
    fun givenUserWithIncorrectPassword_whenLogin_thenThrowsException() {

//        given
        val userRequest = LoginRequest(
            username = "test_username",
            password = "test_incorrect_password"
        )

//        when
        val authentication = UsernamePasswordAuthenticationToken(userRequest.username, userRequest.password)
        val exception = BadCredentialsException("Indifferently")
        `when`(authenticationManager.authenticate(authentication)).thenThrow(exception)

//        then
        assertThrows<BadCredentialsException> { userService.login(userRequest.username, userRequest.password) }
    }

    @Test
    fun givenValidRefreshToken_whenRefresh_thenNewTokensPair() {

//        given
        val userRequest = RefreshRequest(refreshToken = "test_valid_refresh_token")

//        when
        `when`(jwtTokenProvider.validateRefreshToken(userRequest.refreshToken)).thenReturn(true)

        val extractedUsername = "test_username"
        `when`(jwtTokenProvider.getUsernameFromRefreshToken(userRequest.refreshToken)).thenReturn(extractedUsername)

        val foundUser = UserDTO(
            username = extractedUsername,
            roles = listOf(RoleDTO(type = RoleEntity.Type.ROLE_USER))
        )
        `when`(userRepository.findByUsername(extractedUsername)).thenReturn(foundUser.toEntity())

        val newAccessToken = "test_new_access_token"
        `when`(jwtTokenProvider.generateAccessToken(foundUser.username, foundUser.roles)).thenReturn(newAccessToken)

        val newRefreshToken = "test_new_refresh_token"
        `when`(jwtTokenProvider.generateRefreshToken(foundUser.username)).thenReturn(newRefreshToken)

//        then
        val expectedResponse = RefreshResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )

        assertEquals(expectedResponse, userService.refresh(userRequest.refreshToken))
    }

    @Test
    fun givenInvalidRefreshToken_whenRefresh_thenThrowsException() {

//        given
        val userRequest = RefreshRequest(refreshToken = "test_invalid_refresh_token")

//        when
        `when`(jwtTokenProvider.validateRefreshToken(userRequest.refreshToken)).thenReturn(false)

//        then
        assertThrows<JwtAuthenticationException> { userService.refresh(userRequest.refreshToken) }
    }

    @Test
    fun givenCorrectPassword_whenChangePassword_thenReturnsOK() {

//        given
        val accessToken = "test_access_token"
        val oldPassword = "test_password"
        val newPassword = "test_new_password"
        val newPasswordConfirmation = "test_new_password"

//        when
        val foundUser = UserDTO(
            username = "test_username",
            password = "test_encoded_password"
        )
        `when`(jwtTokenProvider.getUsernameFromAccessToken(accessToken)).thenReturn(foundUser.username)
        `when`(userRepository.findByUsername(foundUser.username)).thenReturn(foundUser.toEntity())
        `when`(passwordEncoder.matches(oldPassword, foundUser.password)).thenReturn(true)

        val encodedNewPassword = "test_encoded_new_password"
        `when`(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword)

//        then
        val expectedResponse = ChangePasswordResponse(message = "Password was successfully changed")

        assertEquals(
            expectedResponse,
            userService.changePassword(
                accessToken = accessToken,
                oldPassword = oldPassword,
                newPassword = newPassword,
                newPasswordConfirmation = newPasswordConfirmation
            )
        )
    }

    @Test
    fun givenIncorrectPassword_whenChangePassword_thenThrowsException() {

//        given
        val accessToken = "test_access_token"
        val oldPassword = "wrong_old_password"
        val newPassword = "new_password"
        val newPasswordConfirmation = "new_password"

//        when
        val foundUser = UserDTO(
            username = "test_username",
            password = "test_encoded_password"
        )
        `when`(jwtTokenProvider.getUsernameFromAccessToken(accessToken)).thenReturn(foundUser.username)
        `when`(userRepository.findByUsername(foundUser.username)).thenReturn(foundUser.toEntity())
        `when`(passwordEncoder.matches(oldPassword, foundUser.password)).thenReturn(false)

//        then
        assertThrows<ChangePasswordException> {
            userService.changePassword(
                accessToken = accessToken,
                oldPassword = oldPassword,
                newPassword = newPassword,
                newPasswordConfirmation = newPasswordConfirmation
            )
        }
    }

    @Test
    fun givenDifferentNewPasswordAndConfirmation_whenChangePassword_thenThrowsException() {

//        given
        val accessToken = "test_access_token"
        val oldPassword = "test_password"
        val newPassword = "new_password"
        val newPasswordConfirmation = "wrong_new_password"

//        when
//        then
        assertThrows<ChangePasswordException> {
            userService.changePassword(
                accessToken = accessToken,
                oldPassword = oldPassword,
                newPassword = newPassword,
                newPasswordConfirmation = newPasswordConfirmation
            )
        }
    }
}