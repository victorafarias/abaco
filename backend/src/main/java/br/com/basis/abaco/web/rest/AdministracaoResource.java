package br.com.basis.abaco.web.rest;

import br.com.basis.abaco.service.AdministracaoService;
import br.com.basis.abaco.service.dto.BatchJobExecutionDTO;
import com.codahale.metrics.annotation.Timed;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AdministracaoResource {

    private final AdministracaoService administracaoService;

    public AdministracaoResource(AdministracaoService administracaoService) {
        this.administracaoService = administracaoService;
    }

    @Timed
    @GetMapping("/administracao")
    public List<BatchJobExecutionDTO> obterTodasRotinas() {
        return administracaoService.obterTodasRotinas();
    }
}
