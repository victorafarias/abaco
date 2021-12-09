package br.com.basis.abaco.web.rest;

import br.com.basis.abaco.domain.NovidadesVersao;
import br.com.basis.abaco.service.NovidadesVersaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api")
public class NovidadesVersaoResource {

    private final Logger log = LoggerFactory.getLogger(NovidadesVersao.class);
    private static final String ENTITY_NAME = "Novidades Versão";

    private final NovidadesVersaoService novidadesVersaoService;

    public NovidadesVersaoResource(NovidadesVersaoService novidadesVersaoService) {

        this.novidadesVersaoService = novidadesVersaoService;
    }

    @GetMapping("/novidades-versao")
    public ResponseEntity<Set<NovidadesVersao>> getAllNovidadesVersao(){
        return new ResponseEntity(novidadesVersaoService.getAllNovidadesVersao(), HttpStatus.OK);
    }

    @GetMapping("/novidades-versao/desabilitar-novidades")
    public ResponseEntity<Void> desabilitarNovidadeUsuario(){
        novidadesVersaoService.desabilitarNovidadeUsuario();
        return new ResponseEntity(HttpStatus.OK);
    }

}
