package com.example.back.config;

import java.util.Arrays;

import com.example.back.repository.UserInformationRepository;
import com.example.back.security.AuthProvider;
import com.example.back.security.CustomUserDetailService;
import com.example.back.security.JwtAuthenticationEntryPoint;
import com.example.back.security.JwtAuthenticationFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.AllArgsConstructor;


@Configuration
@EnableWebSecurity
@AllArgsConstructor
@ComponentScan(basePackages = {"com.example.back.security"})
public class AuthConfig extends WebSecurityConfigurerAdapter{

    @Autowired
	private JwtAuthenticationEntryPoint unauthorizedHandler;
	
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private CustomUserDetailService customUserDetailService;

    @Autowired
    private AuthProvider authProvider;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Autowired
	public AuthConfig(UserInformationRepository userInfoRepository, CustomUserDetailService customUserDetailService,
		JwtAuthenticationEntryPoint unauthorizedHandler, JwtAuthenticationFilter jwtAuthenticationFilter, AuthProvider authProvider) {
		this.customUserDetailService = customUserDetailService;
		this.unauthorizedHandler = unauthorizedHandler;
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authProvider = authProvider;
	}

    @Bean(name=BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception{
        return super.authenticationManagerBean();
    }

    //cors setting, ??????, ?????? ?????? ??? ??????????????? ???????????? ??????
    @Bean
    public CorsConfigurationSource corsConfigurationSource(){

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    
    @Override
    // http ?????? ?????? ?????? (??????!)
    protected void configure(HttpSecurity http) throws Exception{
        System.out.println("http Configure");

        http
            .cors()
            .configurationSource(corsConfigurationSource())
            .and()
            .csrf().disable()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers("/**").permitAll()
            .antMatchers("/auth/**").permitAll()
            .anyRequest().authenticated();
            //.and()
            // formLogin??? ????????? authProvider??? ?????????
            // form login??? ?????? UsernamePasswordAuthenticationFilter ?????????
         


        // http
        // .cors()
        // .and()
        // .csrf()
        // .and()
        //     .authorizeRequests()
        // //?????? ?????????????????? ????????? ??????
        //     .antMatchers("/posts/free").permitAll()
        //     //?????? ???????????? ???????????? ????????? ????????? ??????
        //     .antMatchers("/posts/charge").access("hasRole('ADMIN') or hasRole('SUBSCRIBER') or hasRole('PUBLISHER')")
        //     //????????? ???????????? ????????????
        //     .antMatchers("/admin/**").hasRole("ADMIN")
        //     //????????? ???????????? ?????? ????????? ok
        //     .anyRequest().authenticated()
        // .and()
        // .formLogin()
        //     .loginProcessingUrl("/login")
        //     .permitAll();
        // // .and()
        // //     .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);
       
        // http.httpBasic();

    }

    @Override
    @Autowired
    //?????? ????????? ????????? ??????
    //?????? ????????? ???????????? Authentication Manager??? ????????????. UserDetailService??? ?????? ????????? ????????? customUserDetailSercie??? ??????
    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
        System.out.println("configure");
        //auth.authenticationProvider(authProvider);
        try{

            auth.authenticationProvider(this.authProvider);
            auth.userDetailsService(this.customUserDetailService);
        }
        catch(Exception e){
            System.out.println("EROOOOR :" + e.getMessage());
        }
    }


    @Override
    // ?????????, ??????????????????, css ???????????? ?????? ??????
    public void configure(WebSecurity web){
        System.out.println("WebSec Configure");
        web.ignoring().antMatchers("/js/**", "/css/**", "/images/**", "/font/**", "/html/**",
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/configuration/security",
                "/swagger-ui.html"
        
        );
    }

}
