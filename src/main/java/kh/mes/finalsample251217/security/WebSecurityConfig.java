package kh.mes.finalsample251217.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
//                .cors(Customizer.withDefaults()) // CORS 기본 설정
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 무상태성 유지
//                .exceptionHandling(exception -> exception
//                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
//                        .accessDeniedHandler(jwtAccessDeniedHandler)
//                )
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/auth/**").permitAll() // 로그인, 회원가입 허용
//                        // MES 특화 권한 설정
//                        .requestMatchers("/api/mes/order/**").hasRole("ADMIN") // 관리자만 지시 가능
//                        .requestMatchers("/api/mes/material/**").hasRole("ADMIN") // 관리자만 자재 입고 가능
//                        .requestMatchers("/api/mes/machine/**").hasAnyRole("OPERATOR", "ADMIN") // 작업자/관리자 실적 보고 가능
//                        .anyRequest().authenticated()
//                )
//                .apply(new JwtSecurityConfig(tokenProvider));
//
//        return http.build();
//    }
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // [수정] 모든 MES API를 로그인 없이 허용
                    .requestMatchers("/api/mes/**", "/auth/**").permitAll()
                    .anyRequest().authenticated()
            )
            // JWT 필터는 유지해도 되지만, 토큰이 없으면 그냥 통과하게 됩니다.
            .with(new JwtSecurityConfig(tokenProvider), Customizer.withDefaults());

        return http.build();
    }

}
