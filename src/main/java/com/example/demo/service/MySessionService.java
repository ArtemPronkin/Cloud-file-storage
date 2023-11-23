package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;

import java.security.Principal;
import java.util.Collection;
import java.util.Set;

public class MySessionService {
    @Autowired
    public FindByIndexNameSessionRepository<? extends Session> sessions;

    public Collection<? extends Session> getSessions(Principal principal) {
        Collection<? extends Session> usersSessions = this.sessions.findByPrincipalName(principal.getName()).values();
        return usersSessions;
    }

    public void removeSession(Principal principal, String sessionIdToDelete) {
        Set<String> usersSessionIds = this.sessions.findByPrincipalName(principal.getName()).keySet();
        if (usersSessionIds.contains(sessionIdToDelete)) {
            this.sessions.deleteById(sessionIdToDelete);
        }
    }
}
