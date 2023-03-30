package rhino10001.todolist.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.web.SecurityFilterChain


@Configuration
@EnableWebSecurity
class SpringSecurityConfiguration {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf().disable()
            .authorizeHttpRequests()
            .requestMatchers(HttpMethod.GET, "/hello").permitAll()
            .requestMatchers(HttpMethod.POST, "/registration").permitAll()
            .anyRequest()
            .authenticated()
            .and()
            .formLogin()
            .and()
            .httpBasic()
        return http.build()
    }

//    @Bean
//    fun passwordEncoder() = BCryptPasswordEncoder(13)

    @Bean
    fun passwordEncoder(): NoOpPasswordEncoder? {
        return NoOpPasswordEncoder.getInstance() as NoOpPasswordEncoder
    }
}