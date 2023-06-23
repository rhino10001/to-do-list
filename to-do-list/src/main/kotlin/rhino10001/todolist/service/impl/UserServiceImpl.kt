package rhino10001.todolist.service.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import rhino10001.todolist.dto.UserDTO
import rhino10001.todolist.dto.response.ChangePasswordResponse
import rhino10001.todolist.dto.response.LoginResponse
import rhino10001.todolist.dto.response.RefreshResponse
import rhino10001.todolist.dto.toEntity
import rhino10001.todolist.exception.ChangePasswordException
import rhino10001.todolist.exception.JwtAuthenticationException
import rhino10001.todolist.model.RoleEntity
import rhino10001.todolist.model.toDTO
import rhino10001.todolist.repository.RoleRepository
import rhino10001.todolist.repository.UserRepository
import rhino10001.todolist.security.JwtTokenProvider
import rhino10001.todolist.service.UserService

@Service
class UserServiceImpl @Autowired constructor(
    val userRepository: UserRepository,
    val roleRepository: RoleRepository,
    val passwordEncoder: PasswordEncoder,
    val authenticationManager: AuthenticationManager,
    val jwtTokenProvider: JwtTokenProvider
) : UserService {

    override fun register(user: UserDTO): UserDTO {
        val roleUser = roleRepository.findByType(RoleEntity.Type.ROLE_USER).toDTO()
        val copy = user.copy(
            roles = listOf(roleUser),
            password = passwordEncoder.encode(user.password)
        )
        return userRepository.save(copy.toEntity()).toDTO()
    }

    override fun login(username: String, password: String): LoginResponse {
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken(username, password))
        val userDTO = userRepository.findByUsername(username).toDTO()
        return LoginResponse(
            username = userDTO.username,
            accessToken = jwtTokenProvider.generateAccessToken(userDTO.username, userDTO.roles),
            refreshToken = jwtTokenProvider.generateRefreshToken(userDTO.username)
        )
    }

    override fun refresh(refreshToken: String): RefreshResponse {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw JwtAuthenticationException("Invalid refresh token")
        }
        val userDTO = userRepository.findByUsername(jwtTokenProvider.getUsernameFromRefreshToken(refreshToken)).toDTO()
        return RefreshResponse(
            accessToken = jwtTokenProvider.generateAccessToken(userDTO.username, userDTO.roles),
            refreshToken = jwtTokenProvider.generateRefreshToken(userDTO.username)
        )
    }

    override fun changePassword(
        accessToken: String,
        oldPassword: String,
        newPassword: String,
        newPasswordConfirmation: String
    ): ChangePasswordResponse {

        if (newPassword != newPasswordConfirmation) {
            throw ChangePasswordException("New password is not equal to confirmation")
        }

        val username = jwtTokenProvider.getUsernameFromAccessToken(accessToken)
        val userEntity = userRepository.findByUsername(username)

        if (passwordEncoder.matches(oldPassword, userEntity.password)) {
            userEntity.password = passwordEncoder.encode(newPassword)
            userRepository.save(userEntity)
            return ChangePasswordResponse("Password was successfully changed")
        } else {
            throw ChangePasswordException("Incorrect old password")
        }
    }
}