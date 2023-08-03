package rhino10001.todolist.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import rhino10001.todolist.model.RoleEntity

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoleRepositoryTest @Autowired constructor(
    private val roleRepository: RoleRepository
) {

    @Test
    fun givenRoleAdmin_whenFindByType_thenReturnsRoleAdmin() {

//        given
        val roleTypeAdmin = RoleEntity.Type.ROLE_ADMIN

//        when
        val actual = roleRepository.findByType(roleTypeAdmin)

//        then
        val expected = RoleEntity(
            id = 1,
            type = RoleEntity.Type.ROLE_ADMIN
        )

        assertEquals(expected, actual)
    }

    @Test
    fun givenRoleUser_whenFindByType_thenReturnsRoleUser() {

//        given
        val roleTypeUser = RoleEntity.Type.ROLE_USER

//        when
        val actual = roleRepository.findByType(roleTypeUser)

//        then
        val expected = RoleEntity(
            id = 2,
            type = RoleEntity.Type.ROLE_USER
        )

        assertEquals(expected, actual)
    }
}
