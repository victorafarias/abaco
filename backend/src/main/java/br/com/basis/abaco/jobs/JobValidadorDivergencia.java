package br.com.basis.abaco.jobs;

import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.domain.Status;
import br.com.basis.abaco.repository.StatusRepository;
import br.com.basis.abaco.service.AnaliseService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class JobValidadorDivergencia {

    private final AnaliseService analiseService;
    private final StatusRepository statusRepository;

    public JobValidadorDivergencia(AnaliseService analiseService, StatusRepository statusRepository) {
        this.analiseService = analiseService;
        this.statusRepository = statusRepository;
    }

    @Scheduled(cron = "${application.cronAtualizacaoValidacaoDivergencia}")
    public void aprovaDivergenciasForaDoPrazo() {
        List<Analise> analises = analiseService.obterAnalisesDivergenciaForaDoPrazo();
        analises.forEach(analise -> {
            Status status = statusRepository.findByNome("Aprovada");
            analise.setDtEncerramento(new Timestamp(new Date().getTime()));
            analise.setIsEncerrada(true);
            analise.setStatus(status);
            analiseService.salvarAnalise(analise);
        });
    }
}
