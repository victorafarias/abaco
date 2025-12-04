package br.com.basis.abaco.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Implementation of AuditorAware based on Spring Security.
 */
@Component
public class SpringSecurityUserAuditorAware implements AuditorAware<String> {

    @Override
    public String getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        String retorno;
        if (principal instanceof UserDetailsCustom) {
            UserDetailsCustom userDetails = (UserDetailsCustom) principal;
            retorno = userDetails.getUsername();
        } else {
            retorno = String.valueOf(principal);
        }
        return retorno;
    }


}
