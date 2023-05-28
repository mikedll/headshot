
package com.mikedll.headshot;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Authentication;

public class Session implements Authentication {
    private static final long serialVersionUID = 1L;

    public String oauth2State;
    
    public Session principal;
    
    public boolean trusted;

    @Override
    public boolean isAuthenticated() {
        return trusted;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.trusted = isAuthenticated;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return Collections.EMPTY_LIST;
    }
}
