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
import br.com.basis.abaco.service.exception.RelatorioException;
import br.com.basis.abaco.service.relatorio.RelatorioHistoricoColunas;
import br.com.basis.abaco.service.relatorio.RelatorioPerfilColunas;
import br.com.basis.abaco.utils.AbacoUtil;
import br.com.basis.dynamicexports.service.DynamicExportsService;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class HistoricoService {

    private final HistoricoRepository historicoRepository;
    private final UserRepository userRepository;
    private final AnaliseService analiseService;
    private final DynamicExportsService dynamicExportsService;

    public HistoricoService(HistoricoRepository historicoRepository, UserRepository userRepository, AnaliseService analiseService, DynamicExportsService dynamicExportsService) {
        this.historicoRepository = historicoRepository;
        this.userRepository = userRepository;
        this.analiseService = analiseService;
        this.dynamicExportsService = dynamicExportsService;
    }

    public List<HistoricoDTO> findAllByAnalise(Long idAnalise){
        if(idAnalise != null){
            List<Historico> historicos = historicoRepository.findAllByAnaliseIdOrderById(idAnalise);
            if(historicos.size() > 0){
                List<HistoricoDTO> listaHistoricoDTO = new ArrayList<>();
                for (Historico historico : historicos) {
                    AnaliseDTO analiseDTO = analiseService.convertToDto(historico.getAnalise());
                    UserDTO userDTO = new UserDTO(historico.getUsuario());
                    HistoricoDTO historicoDTO = new HistoricoDTO();
                    historicoDTO.setAcao(historico.getAcao());
                    historicoDTO.setAnalise(analiseDTO);
                    historicoDTO.setDtAcao(historico.getDtAcao());
                    historicoDTO.setUsuario(userDTO);
                    historicoDTO.setId(historico.getId());
                    listaHistoricoDTO.add(historicoDTO);
                }
                return listaHistoricoDTO;
            }
        }
        return new ArrayList<>();
    }

    public void inserirHistoricoAnalise(Analise analise, User usuario, String acao){
        if(usuario == null){
            usuario = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).orElse(new User());
        }
        Historico historico = new Historico();
        historico.setAnalise(analise);
        historico.setDtAcao(Timestamp.from(Instant.now()));
        historico.setUsuario(usuario);
        historico.setAcao(acao);
        historicoRepository.save(historico);
    }

    public ByteArrayOutputStream gerarRelatorio(AnaliseDTO analise, String tipoRelatorio) throws RelatorioException {
        ByteArrayOutputStream byteArrayOutputStream;
        try {
            Page<Historico> result = new PageImpl<>(historicoRepository.findAllByAnaliseIdOrderById(analise.getId()));
            byteArrayOutputStream = dynamicExportsService.export(new RelatorioHistoricoColunas(), result, tipoRelatorio, Optional.empty(), Optional.ofNullable(AbacoUtil.REPORT_LOGO_PATH), Optional.ofNullable(AbacoUtil.getReportFooter()));
        } catch (DRException | ClassNotFoundException | JRException | NoClassDefFoundError e) {
            throw new RelatorioException(e);
        }
        return byteArrayOutputStream;
    }


}
