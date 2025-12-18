package br.com.basis.abaco.service;

import br.com.basis.abaco.domain.BaseLineAnaliticoFD;
import br.com.basis.abaco.domain.BaseLineAnaliticoFT;
import br.com.basis.abaco.domain.BaseLineSintetico;
import br.com.basis.abaco.repository.BaseLineAnaliticoFDRepository;
import br.com.basis.abaco.repository.BaseLineAnaliticoFTRepository;
import br.com.basis.abaco.repository.BaseLineSinteticoRepository;
import br.com.basis.abaco.repository.search.BaseLineAnaliticoFDSearchRepository;
import br.com.basis.abaco.repository.search.BaseLineAnaliticoFTSearchRepository;
import br.com.basis.abaco.repository.search.BaseLineSinteticoSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@Service
public class BaselineSinteticoService {

    // Logger para logs estruturados de debugging
    private static final Logger log = LoggerFactory.getLogger(BaselineSinteticoService.class);

    private static final String SELECT_FUNCTION_FD = "SELECT f.row_number, f.classificacao, f.impacto , f.analise_id, f.data_homologacao_software, f.id_sistema, f.nome, f.sigla, f.name , f.pf, f.id_funcao_dados, f.complexidade, f.nome_equipe, f.equipe_responsavel_id, f.nome_funcionalidade, f.nome_modulo, f.der, f.rlr_alr, f.row_number, f.classificacao, f.impacto , f.analise_id, f.data_homologacao_software, f.id_sistema, f.nome, f.sigla, f.name, f.pf, f.id_funcao_dados, f.complexidade, f.nome_equipe, f.equipe_responsavel_id, f.nome_funcionalidade, f.nome_modulo, f.der, f.rlr_alr from fc_funcao_dados_vw( :id , :idEquipe ) f;";
    private static final String SELECT_FUNCTION_FT = "SELECT f.row_number, f.classificacao, f.impacto , f.analise_id, f.data_homologacao_software, f.id_sistema, f.nome, f.sigla, f.name , f.pf, f.id_funcao_dados, f.complexidade, f.nome_equipe, f.equipe_responsavel_id, f.nome_funcionalidade, f.nome_modulo, f.der, f.rlr_alr, f.row_number, f.classificacao, f.impacto , f.analise_id, f.data_homologacao_software, f.id_sistema, f.nome, f.sigla, f.name, f.pf, f.id_funcao_dados, f.complexidade, f.nome_equipe, f.equipe_responsavel_id, f.nome_funcionalidade, f.nome_modulo, f.der, f.rlr_alr from fc_funcao_transacao_vw( :id , :idEquipe ) f; ";
    private static final String SELECT_SINTETICO = "SELECT f.row_number, f.id_sistema, f.sigla, f.nome, f.numero_ocorrencia, f.sum, f.equipe_responsavel_id, f.nome_equipe from fc_baseline_sintetico_vw( :id , :idEquipe ) f;";
    private static final String ID_EQUIPE = "idEquipe";
    private static final String ID = "id";
    private final BaseLineSinteticoSearchRepository baseLineSinteticoSearchRepository;
    private final BaseLineAnaliticoFDSearchRepository baseLineAnaliticoFDSearchRepository;
    private final BaseLineAnaliticoFTSearchRepository baseLineAnaliticoFTSearchRepository;
    private final EntityManager entityManager;

    public BaselineSinteticoService(BaseLineSinteticoSearchRepository baseLineSinteticoSearchRepository,
                                    BaseLineAnaliticoFDSearchRepository baseLineAnaliticoFDSearchRepository,
                                    BaseLineAnaliticoFDRepository baseLineAnaliticoFDRepository,
                                    BaseLineAnaliticoFTSearchRepository baseLineAnaliticoFTSearchRepository,
                                    BaseLineAnaliticoFTRepository baseLineAnaliticoFTRepository,
                                    BaseLineSinteticoRepository baseLineSinteticoRepository,
                                    EntityManager entityManager) {
        this.baseLineSinteticoSearchRepository = baseLineSinteticoSearchRepository;
        this.baseLineAnaliticoFDSearchRepository = baseLineAnaliticoFDSearchRepository;
        this.baseLineAnaliticoFTSearchRepository = baseLineAnaliticoFTSearchRepository;
        this.entityManager = entityManager;
    }


    public BaseLineSintetico getBaseLineSintetico(Long id, Long idEquipe) {
        // Log de entrada para debugging
        log.debug("[BASELINE] Consultando baseline sintético: sistemaId={}, equipeId={}", id, idEquipe);
        
        Query query = entityManager.createNativeQuery(SELECT_SINTETICO, BaseLineSintetico.class)
            .setParameter(ID, id)
            .setParameter(ID_EQUIPE, idEquipe);
        
        // Usando getResultList() para evitar NoResultException quando não há dados
        List<BaseLineSintetico> resultList = query.getResultList();
        
        // Verifica se há resultados
        if (resultList.isEmpty()) {
            log.warn("[BASELINE] Nenhum dado encontrado para baseline sintético: sistemaId={}, equipeId={}. "
                + "Verifique se existem análises bloqueadas e homologadas para este sistema e equipe.", 
                id, idEquipe);
            return null;
        }
        
        BaseLineSintetico baseLineSintetico = resultList.get(0);
        log.debug("[BASELINE] Baseline sintético encontrado: id={}, sistema={}", 
            baseLineSintetico.getId(), baseLineSintetico.getSigla());
        
        return baseLineSintetico;
    }

    public BaseLineSintetico getBaseLineAnaliticoFDFT(Long id, Long idEquipe, BaseLineSintetico baseLineSintetico) {
        baseLineSinteticoSearchRepository.deleteByIdsistemaAndEquipeResponsavelId(id, idEquipe);
        BaseLineSintetico result = baseLineSinteticoSearchRepository.save(baseLineSintetico);
        baseLineAnaliticoFDSearchRepository.deleteAllByIdsistemaAndEquipeResponsavelId(id, idEquipe);
        baseLineAnaliticoFTSearchRepository.deleteAllByIdsistemaAndEquipeResponsavelId(id, idEquipe);
        Query nativeQueryFD = entityManager.createNativeQuery(SELECT_FUNCTION_FD, BaseLineAnaliticoFD.class).setParameter(ID, id).setParameter(ID_EQUIPE, idEquipe);
        Query nativeQueryFT = entityManager.createNativeQuery(SELECT_FUNCTION_FT, BaseLineAnaliticoFT.class).setParameter(ID, id).setParameter(ID_EQUIPE, idEquipe);
        List<BaseLineAnaliticoFT> lstAnaliticoFT = nativeQueryFT.getResultList();
        List<BaseLineAnaliticoFD> lstAnaliticoFD = nativeQueryFD.getResultList();
        baseLineAnaliticoFDSearchRepository.save(lstAnaliticoFD);
        baseLineAnaliticoFTSearchRepository.save(lstAnaliticoFT);
        return result;
    }
}

