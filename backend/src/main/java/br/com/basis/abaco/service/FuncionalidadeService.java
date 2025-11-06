package br.com.basis.abaco.service;

import br.com.basis.abaco.domain.FuncaoDados;
import br.com.basis.abaco.domain.FuncaoTransacao;
import br.com.basis.abaco.domain.Funcionalidade;
import br.com.basis.abaco.domain.Modulo;
import br.com.basis.abaco.domain.Sistema;
import br.com.basis.abaco.domain.Status;
import br.com.basis.abaco.repository.FuncaoDadosRepository;
import br.com.basis.abaco.repository.FuncaoTransacaoRepository;
import br.com.basis.abaco.repository.FuncionalidadeRepository;
import br.com.basis.abaco.repository.ModuloRepository;
import br.com.basis.abaco.repository.SistemaRepository;
import br.com.basis.abaco.repository.search.FuncionalidadeSearchRepository;
import br.com.basis.abaco.repository.search.ModuloSearchRepository;
import br.com.basis.abaco.repository.search.SistemaSearchRepository;
import br.com.basis.abaco.service.dto.DropdownDTO;
import br.com.basis.abaco.service.dto.filter.SearchFilterDTO;
import br.com.basis.abaco.service.exception.RelatorioException;
import br.com.basis.abaco.service.relatorio.RelatorioFuncionalidadeColunas;
import br.com.basis.abaco.service.relatorio.RelatorioStatusColunas;
import br.com.basis.abaco.utils.AbacoUtil;
import br.com.basis.dynamicexports.service.DynamicExportsService;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

@Service
@Transactional
public class FuncionalidadeService {

    private final FuncionalidadeRepository funcionalidadeRepository;
    private final FuncionalidadeSearchRepository funcionalidadeSearchRepository;
    private final FuncaoDadosRepository funcaoDadosRepository;
    private final FuncaoTransacaoRepository funcaoTransacaoRepository;

    private final DynamicExportsService dynamicExportsService;
    private final ModuloRepository moduloRepository;
    private final SistemaRepository sistemaRepository;
    private final ModuloSearchRepository moduloSearchRepository;
    private final SistemaSearchRepository sistemaSearchRepository;

    public FuncionalidadeService(FuncionalidadeRepository funcionalidadeRepository, FuncionalidadeSearchRepository funcionalidadeSearchRepository, FuncaoTransacaoRepository funcaoTransacaoRepository, FuncaoDadosRepository funcaoDadosRepository, DynamicExportsService dynamicExportsService,
                                 ModuloRepository moduloRepository,
                                 SistemaRepository sistemaRepository,
                                 ModuloSearchRepository moduloSearchRepository,
                                 SistemaSearchRepository sistemaSearchRepository) {
        this.funcionalidadeRepository = funcionalidadeRepository;
        this.funcionalidadeSearchRepository = funcionalidadeSearchRepository;
        this.funcaoTransacaoRepository = funcaoTransacaoRepository;
        this.funcaoDadosRepository = funcaoDadosRepository;
        this.dynamicExportsService = dynamicExportsService;
        this.moduloRepository = moduloRepository;
        this.sistemaRepository = sistemaRepository;
        this.moduloSearchRepository = moduloSearchRepository;
        this.sistemaSearchRepository = sistemaSearchRepository;
    }

    @Transactional(readOnly = true)
    public List<DropdownDTO> findDropdownByModuloId(Long idModulo) {
        return funcionalidadeRepository.findDropdownByModuloId(idModulo);
    }

    public Long countTotalFuncao(Long id){
        return this.funcaoDadosRepository.countByFuncionalidadeId(id) + this.funcaoTransacaoRepository.countByFuncionalidadeId(id);
    }

    public void migrarFuncoes(Long idEdit, Long idMigrar){
        Funcionalidade funcionalidadeMigrar = funcionalidadeRepository.findOne(idMigrar);
        Optional<List<FuncaoDados>> funcaoDados = funcaoDadosRepository.findAllByFuncionalidadeId(idEdit);
        Optional<List<FuncaoTransacao>> funcaoTransacaos = funcaoTransacaoRepository.findAllByFuncionalidadeId(idEdit);
        if(funcaoDados.isPresent()){
            funcaoDados.get().forEach(funcao -> {
                funcao.setFuncionalidade(funcionalidadeMigrar);
            });
            funcaoDadosRepository.save(funcaoDados.get());
        }
        if(funcaoTransacaos.isPresent()){
            funcaoTransacaos.get().forEach(funcao -> {
                funcao.setFuncionalidade(funcionalidadeMigrar);
            });
            funcaoTransacaoRepository.save(funcaoTransacaos.get());
        }
    }

    public ByteArrayOutputStream gerarRelatorio(SearchFilterDTO filtro, String tipoRelatorio) throws RelatorioException {
        ByteArrayOutputStream byteArrayOutputStream;

        try {
            Page<Funcionalidade> page = new PageImpl<>(filtro.getFuncionalidades(), dynamicExportsService.obterPageableMaximoExportacao(), filtro.getFuncionalidades().size());
            byteArrayOutputStream = dynamicExportsService.export(new RelatorioFuncionalidadeColunas(), page, tipoRelatorio,
                Optional.empty(), Optional.ofNullable(AbacoUtil.REPORT_LOGO_PATH),
                Optional.ofNullable(AbacoUtil.getReportFooter()));
        } catch (DRException | ClassNotFoundException | JRException | NoClassDefFoundError e) {
            throw new RelatorioException(e);
        }

        return byteArrayOutputStream;
    }

    public Funcionalidade criarFuncionalidade(Funcionalidade funcionalidade, Sistema sistema) {
        funcionalidade = funcionalidadeRepository.save(funcionalidade);
        if (funcionalidade.getModulo().getId() == null) {
            Modulo modulo = funcionalidade.getModulo();
            modulo.setSistema(sistema);
            modulo.setFuncionalidades(Collections.singleton(funcionalidade));
            moduloRepository.save(modulo);
            moduloSearchRepository.save(modulo);

            sistema.getModulos().add(modulo);
            sistemaRepository.save(sistema);
            sistemaSearchRepository.save(sistema);
        }

        return funcionalidadeRepository.save(funcionalidade);
    }
}
