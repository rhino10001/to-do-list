package rhino10001.todolist.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetailsService
import rhino10001.todolist.dto.RoleDTO
import java.util.*

@ExtendWith(MockitoExtension::class)
class JwtTokenProviderTest(

    @Mock
    private val userDetailsService: UserDetailsService
) {

    private val jwtTokenProvider = JwtTokenProvider(
        accessSecret = "test_access_secret_length_must_be_gte_" +
                "hash_algorithm_output_length_256_bits_or_32_unicode_symbols_in_our_case",
        accessExpirationTime = 2000,
        refreshSecret = "test_refresh_secret_length_must_be_gte_" +
                "hash_algorithm_output_length_256_bits_or_32_unicode_symbols_in_our_case",
        refreshExpirationTime = 3000,
        userDetailsService = userDetailsService
    )

    @Test
    fun givenUsernameAndRoles_whenGenerateAccessToken_thenReturnsToken() {

//        given
        val givenUsername = "test_username"
        val givenRoles = listOf(RoleDTO(id = 2))

//        when
        val generatedAccessToken = jwtTokenProvider.generateAccessToken(username = givenUsername, roles = givenRoles)

//        then
        val decoder = Base64.getDecoder()
        val split = generatedAccessToken.split(".")
        val header: Map<String, *> = jacksonObjectMapper().readValue(decoder.decode(split[0]))
        val payload: Map<String, *> = jacksonObjectMapper().readValue(decoder.decode(split[1]))

        assertEquals("HS256", header["alg"])
        assertEquals("test_username", payload["sub"])
        assertEquals(listOf("ROLE_USER"), payload["roles"])
    }

    @Test
    fun givenValidToken_whenValidateAccessToken_thenReturnsTrue() {

//        given
        val givenUsername = "test_username"
        val givenRoles = listOf(RoleDTO(id = 2))

//        when
        val generatedAccessToken = jwtTokenProvider.generateAccessToken(username = givenUsername, roles = givenRoles)

//        then
        assertTrue(jwtTokenProvider.validateAccessToken(generatedAccessToken))
    }

    @Test
    fun givenTokenWithCorruptedSymbol_whenValidateAccessToken_thenReturnsFalse() {

//        given
        val givenUsername = "test_username"
        val givenRoles = listOf(RoleDTO(id = 2))

//        when
        val generatedAccessToken = jwtTokenProvider.generateAccessToken(username = givenUsername, roles = givenRoles)

//        then
        for (i in generatedAccessToken.indices) {
            val toReplace = generatedAccessToken[i]
            val replacement = if (toReplace == 'A') 'Z' else 'A'
            val corrupted = generatedAccessToken.substring(0, i) +
                    replacement +
                    generatedAccessToken.substring(i + 1)
            assertFalse(jwtTokenProvider.validateAccessToken(corrupted))
        }
    }

    @Test
    fun givenExpiredToken_whenValidateAccessToken_thenReturnsFalse() {

//        given
        val givenUsername = "test_username"
        val givenRoles = listOf(RoleDTO(id = 2))

//        when
        val generatedAccessToken = jwtTokenProvider.generateAccessToken(username = givenUsername, roles = givenRoles)
        Thread.sleep(jwtTokenProvider.accessExpirationTime)

//        then
        assertFalse(jwtTokenProvider.validateAccessToken(generatedAccessToken))
    }

    @Test
    fun givenAccessToken_whenGetUsernameFromAccessToken_thenReturnsUsername() {

//        given
        val givenUsername = "test_username"
        val givenRoles = listOf(RoleDTO(id = 2))

//        when
        val generatedAccessToken = jwtTokenProvider.generateAccessToken(username = givenUsername, roles = givenRoles)

//        then
        val method = jwtTokenProvider::class.java.getDeclaredMethod("getUsernameFromAccessToken", String::class.java)
        method.isAccessible = true
        assertEquals(givenUsername, method.invoke(jwtTokenProvider, generatedAccessToken))
    }

    @Test
    fun givenUsername_whenGenerateRefreshToken_thenReturnsToken() {

//        given
        val givenUsername = "test_username"

//        when
        val generatedRefreshToken = jwtTokenProvider.generateRefreshToken(username = givenUsername)

//        then
        val decoder = Base64.getDecoder()
        val split = generatedRefreshToken.split(".")
        val header: Map<String, *> = jacksonObjectMapper().readValue(decoder.decode(split[0]))
        val payload: Map<String, *> = jacksonObjectMapper().readValue(decoder.decode(split[1]))

        assertEquals("HS256", header["alg"])
        assertEquals("test_username", payload["sub"])
    }

    @Test
    fun givenValidToken_whenValidateRefreshToken_thenReturnsTrue() {

//        given
        val givenUsername = "test_username"

//        when
        val generatedRefreshToken = jwtTokenProvider.generateRefreshToken(username = givenUsername)

//        then
        assertTrue(jwtTokenProvider.validateRefreshToken(generatedRefreshToken))
    }

    @Test
    fun givenTokenWithCorruptedSymbol_whenValidateRefreshToken_thenReturnsFalse() {

//        given
        val givenUsername = "test_username"

//        when
        val generatedRefreshToken = jwtTokenProvider.generateRefreshToken(username = givenUsername)

//        then
        for (i in generatedRefreshToken.indices) {
            val toReplace = generatedRefreshToken[i]
            val replacement = if (toReplace == 'A') 'Z' else 'A'
            val corrupted = generatedRefreshToken.substring(0, i) +
                    replacement +
                    generatedRefreshToken.substring(i + 1)
            assertFalse(jwtTokenProvider.validateRefreshToken(corrupted))
        }
    }

    @Test
    fun givenExpiredToken_whenValidateRefreshToken_thenReturnsFalse() {

//        given
        val givenUsername = "test_username"

//        when
        val generatedRefreshToken = jwtTokenProvider.generateRefreshToken(username = givenUsername)
        Thread.sleep(jwtTokenProvider.refreshExpirationTime)

//        then
        assertFalse(jwtTokenProvider.validateRefreshToken(generatedRefreshToken))
    }

    @Test
    fun givenRefreshToken_whenGetUsernameFromRefreshToken_thenReturnsUsername() {

//        given
        val givenUsername = "test_username"

//        when
        val generatedRefreshToken = jwtTokenProvider.generateRefreshToken(username = givenUsername)

//        then
        assertEquals(givenUsername, jwtTokenProvider.getUsernameFromRefreshToken(generatedRefreshToken))
    }

    @Test
    fun givenAccessToken_whenGetAuthentication_thenReturnsAuthentication() {

//        given
        val givenUsername = "test_username"
        val givenRoles = listOf(RoleDTO(id = 2))

//        when
        val user = JwtUserDetailsImpl(
            id = 1,
            username = givenUsername,
            password = "test_password",
            roles = givenRoles
        )
        Mockito.`when`(userDetailsService.loadUserByUsername(givenUsername)).thenReturn(user)

        val generatedAccessToken = jwtTokenProvider.generateAccessToken(username = givenUsername, roles = givenRoles)
        val authentication = jwtTokenProvider.getAuthentication(generatedAccessToken)

//        then
        val expected = UsernamePasswordAuthenticationToken(user, "", user.authorities)

        assertEquals(expected, authentication)
    }
}