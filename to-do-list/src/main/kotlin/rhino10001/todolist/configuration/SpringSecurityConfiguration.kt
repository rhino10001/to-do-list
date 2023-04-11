package rhino10001.todolist.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import rhino10001.todolist.exception.ExceptionHandler
import rhino10001.todolist.model.RoleEntity
import rhino10001.todolist.security.JwtTokenFilter
import rhino10001.todolist.security.JwtTokenProvider


@Configuration
@EnableWebSecurity
class SpringSecurityConfiguration @Autowired constructor(
    private val exceptionHandler: ExceptionHandler,
    @Lazy
    private val jwtUtils: JwtTokenProvider
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .addFilterBefore(JwtTokenFilter(jwtUtils), UsernamePasswordAuthenticationFilter::class.java)
        http
            .httpBasic().disable()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests()
            .requestMatchers(HttpMethod.GET, "/api/v0/hello").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/v0/auth/registration").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/v0/auth/login").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/v0/auth/refresh").permitAll()
            .and()
            .authorizeHttpRequests()
            .requestMatchers(HttpMethod.GET, "/api/v0/admin").hasAuthority(RoleEntity.Type.ADMIN.name)
            .anyRequest().authenticated()
            .and()
            .exceptionHandling().authenticationEntryPoint(exceptionHandler).accessDeniedHandler(exceptionHandler)

        return http.build()
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder(13)
    }
}