package br.com.basis.abaco.service;

import br.com.basis.abaco.repository.AdministracaoRepository;
import br.com.basis.abaco.service.dto.BatchJobExecutionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdministracaoService {

    private final AdministracaoRepository administracaoRepository;

    public List<BatchJobExecutionDTO> obterTodasRotinas() {
        return administracaoRepository.obterTodasRotinas();
    }

}
