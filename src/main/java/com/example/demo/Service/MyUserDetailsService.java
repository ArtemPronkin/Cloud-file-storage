package com.example.demo.Service;

import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        var optionalUser = userRepository.findByEmailOrUsername(login, login);
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException(login);
        }

        return new MyPrincipal(optionalUser.get());
    }

    
}
