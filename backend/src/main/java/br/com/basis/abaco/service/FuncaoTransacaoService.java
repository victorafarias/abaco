package br.com.basis.abaco.service;

import br.com.basis.abaco.domain.Alr;
import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.domain.Der;
import br.com.basis.abaco.domain.FatorAjuste;
import br.com.basis.abaco.domain.FuncaoTransacao;
import br.com.basis.abaco.domain.VwAlr;
import br.com.basis.abaco.domain.VwAlrAll;
import br.com.basis.abaco.domain.VwDer;
import br.com.basis.abaco.domain.VwDerAll;
import br.com.basis.abaco.domain.enumeration.MetodoContagem;
import br.com.basis.abaco.domain.enumeration.TipoFatorAjuste;
import br.com.basis.abaco.repository.FatorAjusteRepository;
import br.com.basis.abaco.repository.FuncaoTransacaoRepository;
import br.com.basis.abaco.repository.search.VwAlrAllSearchRepository;
import br.com.basis.abaco.repository.search.VwAlrSearchRepository;
import br.com.basis.abaco.repository.search.VwDerAllSearchRepository;
import br.com.basis.abaco.repository.search.VwDerSearchRepository;
import br.com.basis.abaco.service.dto.FuncaoImportarDTO;
import br.com.basis.abaco.service.dto.ImportarFTDTO;
import br.com.basis.abaco.service.dto.PEAnaliticoDTO;
import br.com.basis.abaco.service.dto.novo.AbacoMensagens;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class FuncaoTransacaoService {

    private final FuncaoTransacaoRepository funcaoTransacaoRepository;

    @Autowired
    private AnaliseService analiseService;

    @Autowired
    private FatorAjusteRepository fatorAjusteRepository;

    @Autowired
    private VwDerAllSearchRepository vwDerAllSearchRepository;

    @Autowired
    private VwAlrAllSearchRepository vwAlrAllSearchRepository;

    @Autowired
    private VwDerSearchRepository vwDerSearchRepository;

    @Autowired
    private VwAlrSearchRepository vwAlrSearchRepository;

    @Autowired
    private ConfiguracaoService configuracaoService;
    public FuncaoTransacaoService(FuncaoTransacaoRepository funcaoTransacaoRepository) {
        this.funcaoTransacaoRepository = funcaoTransacaoRepository;
    }


    public ImportarFTDTO importarFuncaoAnalise(FuncaoImportarDTO funcaoImportarDTO) {
        Analise analise = analiseService.recuperarAnalise(funcaoImportarDTO.getIdAnalise());
        ImportarFTDTO retorno = new ImportarFTDTO();
        AbacoMensagens mensagens = new AbacoMensagens();
        FatorAjuste fator = fatorAjusteRepository.findOne(funcaoImportarDTO.getIdDeflator());
        List<FuncaoTransacao> funcoesAdicionadas = new ArrayList<>();
        for(PEAnaliticoDTO funcao: funcaoImportarDTO.getFuncoesParaImportar()){
            if(this.verificarSeExisteFuncao(funcao, funcaoImportarDTO)){
                mensagens.adicionarNovoErro("Já existe uma função com o nome "+funcao.getName()+" na funcionalidade "+funcao.getNomeFuncionalidade());
                continue;
            }

            FuncaoTransacao funcaoOld = funcaoTransacaoRepository.findOne(funcao.getIdfuncaodados());
            FuncaoTransacao funcaoParaSalvar = new FuncaoTransacao();
            funcaoParaSalvar.bindFuncaoTransacao(funcaoOld.getTipo(), funcaoOld.getFtrStr(), funcaoOld.getQuantidade(), funcaoOld.getAlrs(), funcaoOld.getFiles(), funcaoOld.getFtrValues(), funcaoOld.getImpacto(), funcaoOld.getDers(), null, funcaoOld.getComplexidade(), funcaoOld.getPf(), funcaoOld.getGrossPF(), funcaoOld.getFuncionalidade(), funcaoOld.getDetStr(), funcaoOld.getFatorAjuste(), funcaoOld.getName(), funcaoOld.getSustantation(), funcaoOld.getDerValues(), funcaoOld.getEquipe(), null);
            funcaoParaSalvar.setFuncionalidade(funcaoOld.getFuncionalidade());
            funcaoParaSalvar.setId(null);
            funcaoParaSalvar.setAnalise(analise);
            funcaoParaSalvar.setFatorAjuste(fator);

            funcaoParaSalvar = this.setarFuncaoTransacao(analise, funcaoParaSalvar);

            if(funcaoImportarDTO.getFundamentacao() != null){
                funcaoParaSalvar.setSustantation(funcaoImportarDTO.getFundamentacao());
            }
            if(fator.getTipoAjuste().equals(TipoFatorAjuste.UNITARIO) && funcaoImportarDTO.getQuantidadeINM() != null){
                funcaoParaSalvar.setQuantidade(funcaoParaSalvar.getQuantidade());
            }
;
            FuncaoTransacao result = funcaoTransacaoRepository.save(funcaoParaSalvar);

            if(configuracaoService.buscarConfiguracaoHabilitarCamposFuncao() == true && analise.getMetodoContagem().equals(MetodoContagem.DETALHADA)){
                saveVwDersAndVwAlrs(result.getDers(), result.getAlrs(), analise.getSistema().getId(), result.getId());
            }
            funcoesAdicionadas.add(result);
            mensagens.adicionarNovoSucesso("Função "+result.getName()+" criada com sucesso!");
        }
        retorno.setFuncaoTransacao(funcoesAdicionadas);
        retorno.setAbacoMensagens(mensagens);
        return retorno;
    }

    private FuncaoTransacao setarFuncaoTransacao(Analise analise, FuncaoTransacao funcaoParaSalvar) {
        if(analise.getMetodoContagem().equals(MetodoContagem.DETALHADA)){
            List<Der> ders = new ArrayList<>();
            List<Alr> alrs = new ArrayList<>();
            for (Der der : funcaoParaSalvar.getDers()) {
                Der derNovo = new Der(null, der.getNome(), der.getValor(), null, null, funcaoParaSalvar);
                ders.add(derNovo);
            }
            for (Alr alr : funcaoParaSalvar.getAlrs()) {
                Alr alrNovo = new Alr(null, alr.getNome(), alr.getValor(), funcaoParaSalvar, null);
                alrs.add(alrNovo);
            }
            funcaoParaSalvar.setDers(ders.stream().collect(Collectors.toSet()));
            funcaoParaSalvar.setAlrs(alrs.stream().collect(Collectors.toSet()));
        }else{
            funcaoParaSalvar.setDers(new HashSet<>());
            funcaoParaSalvar.setAlrs(new HashSet<>());
        }
        return funcaoParaSalvar;
    }

    private Boolean verificarSeExisteFuncao(PEAnaliticoDTO funcao, FuncaoImportarDTO funcaoImportarDTO) {
        Boolean existInAnalise = funcaoTransacaoRepository.existsByNameAndAnaliseIdAndFuncionalidadeIdAndFuncionalidadeModuloId(funcao.getName(), funcaoImportarDTO.getIdAnalise(), funcao.getIdFuncionalidade(), funcao.getIdModulo());
        return existInAnalise;
    }

    public void saveVwDersAndVwAlrs(Set<Der> ders, Set<Alr> alrs, Long idSistema, Long idFuncao) {
        List<VwDer> vwDerList = vwDerSearchRepository.findAllByIdSistemaFT(idSistema);
        List<VwAlr> vwAlrList = vwAlrSearchRepository.findAllByIdSistema(idSistema);

        List<VwDerAll> vwDerAllList = vwDerAllSearchRepository.findByFuncaoId(idFuncao);
        List<VwAlrAll> vwAlrAllList = vwAlrAllSearchRepository.findByFuncaoId(idFuncao);

        saveVwDers(ders, vwDerList, idSistema, idFuncao, vwDerAllList);
        saveVwAlrs(alrs, vwAlrList, idSistema, idFuncao, vwAlrAllList);
    }

    private void saveVwAlrs(Set<Alr> alrs, List<VwAlr> vwAlrList, Long idSistema, Long idFuncao, List<VwAlrAll> vwAlrAllList) {
        List<VwAlr> vwAlrs = new ArrayList<>();
        List<VwAlrAll> vwAlrAlls = new ArrayList<>();
        if(!alrs.isEmpty()){
            alrs.forEach(item -> {
                VwAlr vwAlr = new VwAlr();
                VwAlrAll vwAlrAll = new VwAlrAll();
                if(item.getId() != null){
                    vwAlr.setId(item.getId());
                    vwAlrAll.setId(item.getId());
                }
                vwAlr.setNome(item.getNome());
                vwAlr.setIdSistema(idSistema);
                if(!vwAlrList.contains(vwAlr)){
                    vwAlrs.add(vwAlr);
                }

                vwAlrAll.setNome(item.getNome());
                vwAlrAll.setFuncaoId(idFuncao);
                if(!vwAlrAllList.contains(vwAlrAll)){
                    vwAlrAlls.add(vwAlrAll);
                }
            });
            if(!vwAlrs.isEmpty()){
                vwAlrSearchRepository.save(vwAlrs);
            }
            if(!vwAlrAlls.isEmpty()){
                vwAlrAllSearchRepository.save(vwAlrAlls);
            }
        }
    }

    private void saveVwDers(Set<Der> ders, List<VwDer> vwDerList, Long idSistema, Long idFuncao, List<VwDerAll> vwDerAllList) {
        List<VwDer> vwDers = new ArrayList<>();
        List<VwDerAll> vwDerAlls = new ArrayList<>();
        if(!ders.isEmpty()){
            ders.forEach(item -> {
                VwDer vwDer = new VwDer();
                VwDerAll vwDerAll = new VwDerAll();
                if(item.getId() != null){
                    vwDer.setId(item.getId());
                    vwDerAll.setId(item.getId());
                }
                vwDer.setNome(item.getNome());
                vwDer.setIdSistemaFT(idSistema);
                if(!vwDerList.contains(vwDer)){
                    vwDers.add(vwDer);
                }

                vwDerAll.setFuncaoId(idFuncao);
                vwDerAll.setNome(item.getNome());
                if(!vwDerAllList.contains(vwDerAll)){
                    vwDerAlls.add(vwDerAll);
                }
            });
            if(!vwDers.isEmpty()){
                vwDerSearchRepository.save(vwDers);
            }
            if(!vwDerAlls.isEmpty()){
                vwDerAllSearchRepository.save(vwDerAlls);
            }
        }
    }
}
