package com.example.demo.Service;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
public class MyUserDetailsService implements UserDetailsService {


    private UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User loadUserByEmail(String login) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(login).orElseThrow();
        return user;
    }


    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
//        var optionalUser = userRepository.findByUsername(login);
//        if (optionalUser.isEmpty()){
//            optionalUser = userRepository.findByEmail(login);
//        }
        var optionalUser = userRepository.findByEmailOrUsername(login,login);
        if(optionalUser.isEmpty()){
            throw new UsernameNotFoundException(login);
        }

        return new MyPrincipal(optionalUser.get());
    }
}
