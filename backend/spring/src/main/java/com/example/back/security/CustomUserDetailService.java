package com.example.back.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.back.model.user.UserInformation;
import com.example.back.repository.UserInformationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


// 사용자별 데이터를 로드하는 인터페이스
@Service
public class CustomUserDetailService implements UserDetailsService{
    
    // db로 아이디 값을 체크하면 끝

    @Autowired
    UserInformationRepository userInfoRepo;
  
    @Transactional
    public UserDetails loadUserByUsername(Map<String, String> Data) 
                throws UsernameNotFoundException, DataAccessException, BadCredentialsException{
        
        // email로 db 조회

        String email = Data.get("email");
        String password = Data.get("password");

        UserInformation userAuths = userInfoRepo.findByEmail(email).orElseThrow(() -> 
                                    new InternalAuthenticationServiceException("EMAIL_ERROR"));  
                

        if(!userAuths.getPassword().equals(password)){
            throw new BadCredentialsException("PASSWORD_ERROR");
        }
        
        List<SimpleGrantedAuthority> authList = new ArrayList<SimpleGrantedAuthority>();
        //유저 레벨 권한
        authList.add(new SimpleGrantedAuthority("AUTH"));
 
        //엔티티 아님!
        return new User(userAuths.getEmail(), userAuths.getPassword(), authList);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // TODO Auto-generated method stub

        UserInformation userAuths = userInfoRepo.findByEmail(email).orElseThrow(() -> 
        new UsernameNotFoundException("User not found with email : " + email));  

        // if(userAuths == null){
        //     throw new UnauthorizedException(HttpStatus.NOT_FOUND, "없는 이메일입니다.");
        // }

        // if(userAuths.getPassword() == password){
        //     throw new UnauthorizedException(HttpStatus.UNAUTHORIZED, "비밀번호가 맞지 않습니다.");
        // }

        List<SimpleGrantedAuthority> authList = new ArrayList<SimpleGrantedAuthority>();
        //유저 레벨 권한
        authList.add(new SimpleGrantedAuthority("AUTH"));
    
        //엔티티 아님!
        return new User(userAuths.getEmail(), userAuths.getPassword(), authList);
    }
}