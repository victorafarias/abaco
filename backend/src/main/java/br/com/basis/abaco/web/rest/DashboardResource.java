package br.com.basis.abaco.web.rest;

import br.com.basis.abaco.repository.AnaliseRepository;
import br.com.basis.abaco.service.dto.Dashboard2DTO;
import com.codahale.metrics.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DashboardResource {

    @Autowired
    private AnaliseRepository analiseRepository;

    public DashboardResource(AnaliseRepository analiseRepository) {
        this.analiseRepository = analiseRepository;
    }

    @GetMapping("/dashboard2")
    @Timed
    public List<Dashboard2DTO> obterTodosMotivos(){
        return analiseRepository.getMotivosAnalise();
    }

    @GetMapping("/dashboard2/clientes")
    @Timed
    public List<Dashboard2DTO> obterTodosClientes(){
        return analiseRepository.getClientesAnalise();
    }

    @GetMapping("/dashboard2/historico")
    @Timed
    public List<Dashboard2DTO> obterHistoricoDiferenca(){
        return analiseRepository.getHistoricoDiferenca();
    }

    @GetMapping("/dashboard2/totalDemandas")
    @Timed
    public List<Dashboard2DTO> obterTotalDemandasAprovadas(){
        return analiseRepository.getTotalDemandas();
    }

    @GetMapping("/dashboard2/pfDiferencaGlobal")
    @Timed
    public List<Dashboard2DTO> obterpfDiferencaGlobal(){
        return analiseRepository.getHistoricoDiferencaGlobal();
    }
}
