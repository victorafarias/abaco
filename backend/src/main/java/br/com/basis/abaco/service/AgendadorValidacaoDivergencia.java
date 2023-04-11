package br.com.basis.abaco.service;

import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.domain.Status;
import br.com.basis.abaco.repository.AnaliseRepository;
import br.com.basis.abaco.repository.StatusRepository;
import br.com.basis.abaco.repository.search.AnaliseSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendadorValidacaoDivergencia {

    private final StatusRepository statusRepository;
    private final AnaliseService analiseService;

    @Scheduled(cron = "${application.cronAtualizacaoValidacaoDivergencia}")
    public void aprovaDivergenciasForaDoPrazo() {
        List<Analise> analises = analiseService.obterAnalisesDivergenciaForaDoPrazo();
        analises.forEach(analise -> {
            Status status = statusRepository.findByNome("Aprovada").get();
            analise.setDtEncerramento(new Timestamp(new Date().getTime()));
            analise.setIsEncerrada(true);
            analise.setStatus(status);
            analiseService.salvarAnalise(analise);
        });
    }
}
