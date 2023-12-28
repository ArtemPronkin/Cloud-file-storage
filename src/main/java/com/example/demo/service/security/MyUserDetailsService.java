package com.example.demo.service.security;

import com.example.demo.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        var optionalUser = userRepository.findByEmailOrUsername(login, login);
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException(login);
        }

        return new MyPrincipal(optionalUser.get());
    }


}
