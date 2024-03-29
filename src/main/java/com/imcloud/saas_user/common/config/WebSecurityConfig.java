package com.imcloud.saas_user.common.config;

import com.imcloud.saas_user.common.jwt.JwtAuthFilter;
import com.imcloud.saas_user.common.jwt.JwtUtil;
import com.imcloud.saas_user.common.security.CustomAccessDeniedHandler;
import com.imcloud.saas_user.common.security.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;

// TODO: security 더 세부적으로 설정해야함
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity // 스프링 Security 지원을 가능하게 함
@EnableGlobalMethodSecurity(securedEnabled = true) // @Secured 어노테이션 활성화
public class WebSecurityConfig implements WebMvcConfigurer {
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final JwtUtil jwtUtil;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**","/docs","/docs/**")
                .antMatchers("/version")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();

        // 기본 설정인 Session 방식은 사용하지 않고 JWT 방식을 사용하기 위한 설정
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

//        http.headers().frameOptions().disable();

        http.authorizeRequests()
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                .antMatchers(HttpMethod.GET, "/api/health-check").permitAll()
                .antMatchers("/api/members/signup").permitAll()
                .antMatchers("/api/members/login").permitAll()
                .antMatchers("/api/members/resetPassword").permitAll()
                .antMatchers("/api/boards/adminBoards").permitAll()
                .antMatchers("/api/members/userId/duplicate").permitAll()


                .anyRequest().authenticated()
                .and()
                // JWT 인증/인가를 사용하기 위한 설정
                .addFilterBefore(new JwtAuthFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);


        // 401 Error 처리, authentication 즉, 인증과정에서 실패할 시 처리
        http.exceptionHandling().authenticationEntryPoint(customAuthenticationEntryPoint);

        // 403 Error 처리, 인증과는 별개로 추가적인 권한이 충족되지 않는 경우
        http.exceptionHandling().accessDeniedHandler(customAccessDeniedHandler);

        return http.build();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "OPTIONS", "PUT", "DELETE")
                .allowedHeaders("Content-Type", "Authorization")
                .exposedHeaders("Content-Type", "Authorization")
                .allowCredentials(true)
                .maxAge(216000);
    }

    @Bean
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        // TODO: 배포시, 아래 코드 수정 절대적으로 할 것
//        ArrayList<String> allowedOriginList = new ArrayList<>();
//        for (int port = 3000; port <= 3010; ++port) {
//            allowedOriginList.add("http://localhost:"+ port);
//        }
//        allowedOriginList.add("https://open-velog.vercel.app");
//        allowedOriginList.add("https://search-open-velog-elastic-bvtge6dkgdmsr2i3f3kzwo6m2u.ap-northeast-2.es.amazonaws.com");
//        allowedOriginList.add("http://43.131.252.119:8099/");
//        allowedOriginList.add("http://localhost:8800/");
//
//        registry
//                .addMapping("/**") // 프로그램에서 제공하는 URL
//                .allowedOrigins(allowedOriginList.toArray(new String[0])) // 요청을 허용할 출처를 명시, 전체 허용 (가능하다면 목록을 작성한다.
//                .allowedMethods("GET", "POST", "OPTIONS", "PUT", "DELETE") // 어떤 메서드를 허용할 것인지 (GET, POST...)
//                .allowedHeaders("Content-Type", "Authorization") // 어떤 헤더들을 허용할 것인지
//                .exposedHeaders("Content-Type", "Authorization")
//                .allowCredentials(true) // 쿠키 요청을 허용한다(다른 도메인 서버에 인증하는 경우에만 사용해야하며, true 설정시 보안상 이슈가 발생할 수 있다)
//                .maxAge(216000); // preflight 요청에 대한 응답을 브라우저에서 캐싱하는 시간;
//    }

}
