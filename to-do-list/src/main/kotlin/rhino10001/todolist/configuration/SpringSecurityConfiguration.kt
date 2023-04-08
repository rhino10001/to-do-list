package rhino10001.todolist.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import rhino10001.todolist.security.JwtTokenFilter
import rhino10001.todolist.security.JwtUtils


@Configuration
@EnableWebSecurity
class SpringSecurityConfiguration @Autowired constructor(
    private val jwtUtils: JwtUtils
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .httpBasic().disable()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests()
            .requestMatchers(HttpMethod.GET, "/api/v0/hello").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/v0/registration").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/v0/login").permitAll()
            .anyRequest().authenticated()

        http.addFilterBefore(JwtTokenFilter(jwtUtils), UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

    @Bean
    fun passwordEncoder(): NoOpPasswordEncoder? {
        return NoOpPasswordEncoder.getInstance() as NoOpPasswordEncoder
    }
}