package com.trang.gachon.movie.security;

import com.trang.gachon.movie.entity.Account;
import com.trang.gachon.movie.enums.AccountStatus;
import com.trang.gachon.movie.repository.AccountRepository;
import com.trang.gachon.movie.service.CustomUserDetailsService;
import com.trang.gachon.movie.service.authandotpservice.PendingAccountPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Optional;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final AccountRepository accountRepository;
    private final PendingAccountPolicy pendingAccountPolicy;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider((UserDetailsService) userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/home",
                                "/movies/**",
                                "/showtimes/**", "/promotions/**",
                                "/auth/login", "/auth/register",
                                "/auth/verify-otp","/auth/resend-otp", "/auth/forgot-password", "/auth/reset-password",
                                "/auth/403",
                                "/css/**", "/js/**", "/images/**", "/uploads/**"
                        ).permitAll()
                        .requestMatchers("/booking/**")
                        .hasRole("MEMBER")
                        .requestMatchers("/account/info", "/account/update", "/account/change-password", "/account")
                        .hasAnyRole("MEMBER", "EMPLOYEE", "ADMIN")
                        .requestMatchers("/account/booked-ticket", "/account/score-history")
                        .hasRole("MEMBER")
                        .requestMatchers("/ticket/**", "/booking-manage/**")
                        .hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/admin/**", "/cinema-room/**")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {
                            // Guest bấm "Đặt vé" sẽ bị đẩy sang login trước.
                            // Sau khi login, nếu là MEMBER thì nên quay lại đúng màn booking đang dở.
                            // Còn EMPLOYEE / ADMIN vẫn nên đi thẳng về dashboard nghiệp vụ của họ.
                            String targetUrl = "/home";
                            SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);

                            boolean isAdmin = authentication.getAuthorities().stream()
                                    .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
                            boolean isEmployee = authentication.getAuthorities().stream()
                                    .anyMatch(auth -> "ROLE_EMPLOYEE".equals(auth.getAuthority()));
                            boolean isMember = authentication.getAuthorities().stream()
                                    .anyMatch(auth -> "ROLE_MEMBER".equals(auth.getAuthority()));

                            if (isAdmin) {
                                targetUrl = "/admin/movie/list";
                            } else if (isEmployee) {
                                targetUrl = "/booking-manage";
                            } else if (isMember && savedRequest != null) {
                                targetUrl = savedRequest.getRedirectUrl();
                            }

                            response.sendRedirect(targetUrl);
                        })
                        //.failureUrl("/auth/login?error=true")
                        .failureHandler((request, response, exception) -> {
                                    String msg;
                                    String login = request.getParameter("username");
                                    Optional<Account> matchedAccount = login == null
                                            ? Optional.empty()
                                            : accountRepository.findByUserName(login)
                                                    .or(() -> accountRepository.findByEmail(login));

                                    if (matchedAccount.isPresent()
                                            && matchedAccount.get().getAccountStatus() == AccountStatus.PENDING
                                            && !pendingAccountPolicy.isExpired(matchedAccount.get())) {
                                        msg = "Tài khoản chưa xác thực email!";
                                        request.getSession().setAttribute("showVerifyOtpLink", true);
                                        request.getSession().setAttribute("pendingEmail", matchedAccount.get().getEmail());
                                    } else if ("Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.".equals(exception.getMessage())
                                            || (matchedAccount.isPresent()
                                            && matchedAccount.get().getAccountStatus() == AccountStatus.PENDING
                                            && pendingAccountPolicy.isExpired(matchedAccount.get()))) {
                                        msg = "Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.";
                                    } else {
                                        msg = "Tên tài khoản, email hoặc mật khẩu không đúng!";
                                    }
                                    request.getSession().setAttribute("errorMsg", msg);
                                    response.sendRedirect("/auth/login?error=true");
                                })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/auth/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/auth/403")
                );

        return http.build();
    }

}
