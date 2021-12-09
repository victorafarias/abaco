package br.com.basis.abaco.service;

import br.com.basis.abaco.domain.NovidadesVersao;
import br.com.basis.abaco.domain.User;
import br.com.basis.abaco.repository.NovidadesVersaoRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class NovidadesVersaoService {

    private final NovidadesVersaoRepository novidadesVersaoRepository;
    private final UserService userService;

    public NovidadesVersaoService(NovidadesVersaoRepository novidadesVersaoRepository, UserService userService) {
        this.novidadesVersaoRepository = novidadesVersaoRepository;
        this.userService = userService;
    }

    public Set<NovidadesVersao> getAllNovidadesVersao() {
        User user = userService.getLoggedUser();
        if(user.getMostrarNovidades() == null || user.getMostrarNovidades() == true){
            return novidadesVersaoRepository.findAllOrderById();
        }
        return new HashSet<>();
    }

    public void desabilitarNovidadeUsuario() {
        User user = userService.getLoggedUser();
        if(user != null && (user.getMostrarNovidades() == null || user.getMostrarNovidades() == true)){
            user.setMostrarNovidades(false);
            userService.salvarUsuario(user);
        }
    }
}
