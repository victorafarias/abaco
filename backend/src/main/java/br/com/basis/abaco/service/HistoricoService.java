package br.com.basis.abaco.service;

import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.domain.Historico;
import br.com.basis.abaco.domain.User;
import br.com.basis.abaco.repository.HistoricoRepository;
import br.com.basis.abaco.repository.UserRepository;
import br.com.basis.abaco.security.SecurityUtils;
import br.com.basis.abaco.service.dto.AnaliseDTO;
import br.com.basis.abaco.service.dto.HistoricoDTO;
import br.com.basis.abaco.service.dto.UserDTO;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class HistoricoService {

    private final HistoricoRepository historicoRepository;
    private final UserRepository userRepository;
    private final AnaliseService analiseService;

    public HistoricoService(HistoricoRepository historicoRepository, UserRepository userRepository, AnaliseService analiseService) {
        this.historicoRepository = historicoRepository;
        this.userRepository = userRepository;
        this.analiseService = analiseService;
    }

    public List<HistoricoDTO> findAllByAnalise(Long idAnalise){
        if(idAnalise != null){
            List<Historico> historicos = historicoRepository.findAllByAnaliseIdOrderById(idAnalise);
            if(historicos.size() > 0){
                List<HistoricoDTO> listaHistoricoDTO = new ArrayList<>();
                for (Historico historico : historicos) {
                    AnaliseDTO analiseDTO = analiseService.convertToDto(historico.getAnalise());
                    UserDTO userDTO = new UserDTO(historico.getUsuario());
                    HistoricoDTO historicoDTO = new HistoricoDTO(historico.getId(), analiseDTO, historico.getDtAcao(), userDTO, historico.getAcao());
                    listaHistoricoDTO.add(historicoDTO);
                }
                return listaHistoricoDTO;
            }
        }
        return null;
    }

    public void inserirHistoricoAnalise(Analise analise, User usuario, String acao){
        if(usuario == null){
            usuario = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
        }
        Historico historico = new Historico();
        historico.setAnalise(analise);
        historico.setDtAcao(Timestamp.from(Instant.now()));
        historico.setUsuario(usuario);
        historico.setAcao(acao);
        historicoRepository.save(historico);
    }

}
