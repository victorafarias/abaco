package br.com.basis.abaco.service;

import br.com.basis.abaco.domain.FuncaoDados;
import br.com.basis.abaco.domain.FuncaoTransacao;
import br.com.basis.abaco.domain.Funcionalidade;
import br.com.basis.abaco.domain.Status;
import br.com.basis.abaco.repository.FuncaoDadosRepository;
import br.com.basis.abaco.repository.FuncaoTransacaoRepository;
import br.com.basis.abaco.repository.FuncionalidadeRepository;
import br.com.basis.abaco.repository.search.FuncionalidadeSearchRepository;
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

    public FuncionalidadeService(FuncionalidadeRepository funcionalidadeRepository, FuncionalidadeSearchRepository funcionalidadeSearchRepository, FuncaoTransacaoRepository funcaoTransacaoRepository, FuncaoDadosRepository funcaoDadosRepository, DynamicExportsService dynamicExportsService) {
        this.funcionalidadeRepository = funcionalidadeRepository;
        this.funcionalidadeSearchRepository = funcionalidadeSearchRepository;
        this.funcaoTransacaoRepository = funcaoTransacaoRepository;
        this.funcaoDadosRepository = funcaoDadosRepository;
        this.dynamicExportsService = dynamicExportsService;
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
}
