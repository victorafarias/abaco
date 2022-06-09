package br.com.basis.abaco.service;

import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.domain.Der;
import br.com.basis.abaco.domain.FatorAjuste;
import br.com.basis.abaco.domain.FuncaoDados;
import br.com.basis.abaco.domain.Rlr;
import br.com.basis.abaco.domain.UploadedFile;
import br.com.basis.abaco.domain.VwDer;
import br.com.basis.abaco.domain.VwDerAll;
import br.com.basis.abaco.domain.VwRlr;
import br.com.basis.abaco.domain.VwRlrAll;
import br.com.basis.abaco.domain.enumeration.MetodoContagem;
import br.com.basis.abaco.domain.enumeration.TipoFatorAjuste;
import br.com.basis.abaco.repository.FatorAjusteRepository;
import br.com.basis.abaco.repository.FuncaoDadosRepository;
import br.com.basis.abaco.repository.search.VwDerAllSearchRepository;
import br.com.basis.abaco.repository.search.VwDerSearchRepository;
import br.com.basis.abaco.repository.search.VwRlrAllSearchRepository;
import br.com.basis.abaco.repository.search.VwRlrSearchRepository;
import br.com.basis.abaco.service.dto.DropdownDTO;
import br.com.basis.abaco.service.dto.FuncaoImportarDTO;
import br.com.basis.abaco.service.dto.ImportarFDDTO;
import br.com.basis.abaco.service.dto.PEAnaliticoDTO;
import br.com.basis.abaco.service.dto.novo.AbacoMensagens;
import br.com.basis.abaco.web.rest.errors.UploadException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class FuncaoDadosService {

    private final FuncaoDadosRepository funcaoDadosRepository;

    private final AnaliseService analiseService;

    @Autowired
    private FatorAjusteRepository fatorAjusteRepository;


    @Autowired
    private VwRlrAllSearchRepository vwRlrAllSearchRepository;
    @Autowired
    private VwDerAllSearchRepository vwDerAllSearchRepository;

    @Autowired
    private VwDerSearchRepository vwDerSearchRepository;
    @Autowired
    private VwRlrSearchRepository vwRlrSearchRepository;

    @Autowired
    private ConfiguracaoService configuracaoService;

    public FuncaoDadosService(FuncaoDadosRepository funcaoDadosRepository, AnaliseService analiseService) {
        this.funcaoDadosRepository = funcaoDadosRepository;
        this.analiseService = analiseService;
    }

    @Transactional(readOnly = true)
    public List<DropdownDTO> getFuncaoDadosDropdown() {
        return funcaoDadosRepository.getFuncaoDadosDropdown();
    }

    public List<UploadedFile> uploadFiles(List<MultipartFile> files){
        List<UploadedFile> uploadedFiles = new ArrayList<>();
        try {
            for(MultipartFile fileFunc : files) {
                UploadedFile uploadedFileFunc = new UploadedFile();
                byte[] bytes = fileFunc.getBytes();
                byte[] bytesFileName = (fileFunc.getOriginalFilename() + String.valueOf(System.currentTimeMillis()))
                    .getBytes("UTF-8");
                String filename = DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(bytesFileName));
                String ext = FilenameUtils.getExtension(fileFunc.getOriginalFilename());
                filename += "." + ext;
                uploadedFileFunc.setLogo(bytes);
                uploadedFileFunc.setDateOf(new Date());
                uploadedFileFunc.setOriginalName(fileFunc.getOriginalFilename());
                uploadedFileFunc.setFilename(filename);
                uploadedFileFunc.setSizeOf(bytes.length);
                uploadedFiles.add(uploadedFileFunc);
            }
            return uploadedFiles;
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new UploadException("Erro ao efetuar o upload do arquivo", e);
        }
    }

    public Boolean existFuncaoDadosDivergencia(Long idAnalise, Long idfuncionalidade, Long idModulo, String name, Long id, Long idEquipe) {
        Boolean existInAnalise = false;
        if(idEquipe != null && idEquipe > 0){
            if (id != null && id > 0) {
                existInAnalise = funcaoDadosRepository.existsByNameAndAnaliseIdAndFuncionalidadeIdAndFuncionalidadeModuloIdAndIdNotAndEquipeId(name, idAnalise, idfuncionalidade, idModulo, id, idEquipe);
            } else {
                existInAnalise = funcaoDadosRepository.existsByNameAndAnaliseIdAndFuncionalidadeIdAndFuncionalidadeModuloIdAndEquipeId(name, idAnalise, idfuncionalidade, idModulo, idEquipe);
            }
        }else{
            if (id != null && id > 0) {
                existInAnalise = funcaoDadosRepository.existsByNameAndAnaliseIdAndFuncionalidadeIdAndFuncionalidadeModuloIdAndIdNot(name, idAnalise, idfuncionalidade, idModulo, id);
            } else {
                existInAnalise = funcaoDadosRepository.existsByNameAndAnaliseIdAndFuncionalidadeIdAndFuncionalidadeModuloId(name, idAnalise, idfuncionalidade, idModulo);
            }
        }
        return existInAnalise;
    }

    public ImportarFDDTO importarFuncaoAnalise(FuncaoImportarDTO funcaoImportarDTO) {
        Analise analise = analiseService.recuperarAnalise(funcaoImportarDTO.getIdAnalise());
        ImportarFDDTO retorno = new ImportarFDDTO();
        AbacoMensagens mensagens = new AbacoMensagens();
        FatorAjuste fator = fatorAjusteRepository.findOne(funcaoImportarDTO.getIdDeflator());
        List<FuncaoDados> funcoesAdicionadas = new ArrayList<>();
        for(PEAnaliticoDTO funcao: funcaoImportarDTO.getFuncoesParaImportar()){
            if(this.verificarSeExisteFuncao(funcao, funcaoImportarDTO)){
                mensagens.adicionarNovoErro("Já existe uma função com o nome "+funcao.getName()+" na funcionalidade "+funcao.getNomeFuncionalidade());
                continue;
            }

            FuncaoDados funcaoOld = funcaoDadosRepository.findById(funcao.getIdfuncaodados());
            FuncaoDados funcaoParaSalvar = new FuncaoDados();
            funcaoParaSalvar.bindFuncaoDados(funcaoOld.getComplexidade(), funcaoOld.getPf(), funcaoOld.getGrossPF(), null, funcaoOld.getFuncionalidade(), funcaoOld.getDetStr(), funcaoOld.getFatorAjuste(), funcaoOld.getName(), funcaoOld.getSustantation(), funcaoOld.getDerValues(), funcaoOld.getTipo(), funcaoOld.getRetStr(), funcaoOld.getQuantidade(), funcaoOld.getRlrs(), funcaoOld.getAlr(), funcaoOld.getFiles(), funcaoOld.getRlrValues(), funcaoOld.getDers(), funcaoOld.getFuncaoDadosVersionavel(), funcaoOld.getImpacto(), funcaoOld.getEquipe(), funcaoOld.getOrdem());
            funcaoParaSalvar.setFuncionalidade(funcaoOld.getFuncionalidade());
            funcaoParaSalvar.setId(null);
            funcaoParaSalvar.setAnalise(analise);
            funcaoParaSalvar.setFatorAjuste(fator);

            funcaoParaSalvar = this.setarFuncaoDados(analise, funcaoParaSalvar);

            if(funcaoImportarDTO.getFundamentacao() != null){
                funcaoParaSalvar.setSustantation(funcaoImportarDTO.getFundamentacao());
            }
            if(fator.getTipoAjuste().equals(TipoFatorAjuste.UNITARIO) && funcaoImportarDTO.getQuantidadeINM() != null){
                funcaoParaSalvar.setQuantidade(funcaoParaSalvar.getQuantidade());
            }

            FuncaoDados result = funcaoDadosRepository.save(funcaoParaSalvar);

            if(configuracaoService.buscarConfiguracaoHabilitarCamposFuncao() == true && analise.getMetodoContagem().equals(MetodoContagem.DETALHADA)){
                this.saveVwDersAndVwRlrs(result.getDers(), result.getRlrs(), analise.getSistema().getId(), result.getId());
            }
            funcoesAdicionadas.add(result);
            mensagens.adicionarNovoSucesso("Função "+result.getName()+" criada com sucesso!");
        }
        retorno.setFuncaoDados(funcoesAdicionadas);
        retorno.setAbacoMensagens(mensagens);
        return retorno;
    }

    private FuncaoDados setarFuncaoDados(Analise analise, FuncaoDados funcaoParaSalvar) {
        if(analise.getMetodoContagem().equals(MetodoContagem.DETALHADA)){
            List<Der> ders = new ArrayList<>();
            List<Rlr> rlrs = new ArrayList<>();
            for (Der der : funcaoParaSalvar.getDers()) {
                Der derNovo = new Der(null, der.getNome(), der.getValor(), null, funcaoParaSalvar, null);
                ders.add(derNovo);
            }
            for (Rlr alr : funcaoParaSalvar.getRlrs()) {
                Rlr alrNovo = new Rlr(null, alr.getNome(), alr.getValor(), null, funcaoParaSalvar);
                rlrs.add(alrNovo);
            }
            funcaoParaSalvar.setDers(ders.stream().collect(Collectors.toSet()));
            funcaoParaSalvar.setRlrs(rlrs.stream().collect(Collectors.toSet()));
        }else{
            funcaoParaSalvar.setDers(new HashSet<>());
            funcaoParaSalvar.setRlrs(new HashSet<>());
        }
        return funcaoParaSalvar;
    }

    private boolean verificarSeExisteFuncao(PEAnaliticoDTO funcao, FuncaoImportarDTO funcaoDadosImportar) {
        Boolean existeNaAnalise = funcaoDadosRepository.existsByNameAndAnaliseIdAndFuncionalidadeIdAndFuncionalidadeModuloId(funcao.getName(), funcaoDadosImportar.getIdAnalise(), funcao.getIdFuncionalidade(), funcao.getIdModulo());
        return existeNaAnalise;
    }

    public void saveVwDersAndVwRlrs(Set<Der> ders, Set<Rlr> rlrs, Long idSistema, Long idFuncao) {
        List<VwDer> vwDerList = vwDerSearchRepository.findAllByIdSistemaFD(idSistema);
        List<VwRlr> vwRlrList = vwRlrSearchRepository.findAllByIdSistema(idSistema);

        List<VwDerAll> vwDerAllList = vwDerAllSearchRepository.findByFuncaoId(idFuncao);
        List<VwRlrAll> vwRlrAllList = vwRlrAllSearchRepository.findByFuncaoId(idFuncao);

        saveVwDer(ders, vwDerList, idSistema, idFuncao, vwDerAllList);
        saveVwRlr(rlrs, vwRlrList, idSistema, idFuncao, vwRlrAllList);
    }

    private void saveVwRlr(Set<Rlr> rlrs, List<VwRlr> vwRlrList, Long idSistema, Long idFuncao, List<VwRlrAll> vwRlrAllList) {
        List<VwRlr> vwRlrs = new ArrayList<>();
        List<VwRlrAll> vwRlrAlls = new ArrayList<>();
        if(!rlrs.isEmpty()){
            rlrs.forEach(item -> {
                VwRlr vwRlr = new VwRlr();
                VwRlrAll vwRlrAll = new VwRlrAll();
                if(item.getId() != null) {
                    vwRlr.setId(item.getId());
                    vwRlrAll.setId(item.getId());
                }
                vwRlr.setNome(item.getNome());
                vwRlr.setIdSistema(idSistema);
                if(!vwRlrList.contains(vwRlr)){
                    vwRlrs.add(vwRlr);
                }

                vwRlrAll.setNome(item.getNome());
                vwRlrAll.setFuncaoId(idFuncao);
                if(!vwRlrAllList.contains(vwRlrAll)){
                    vwRlrAlls.add(vwRlrAll);
                }
            });
            if(!vwRlrAlls.isEmpty()){
                vwRlrAllSearchRepository.save(vwRlrAlls);
            }
            if(!vwRlrs.isEmpty()){
                vwRlrSearchRepository.save(vwRlrs);
            }
        }
    }

    private void saveVwDer(Set<Der> ders, List<VwDer> vwDerList, Long idSistema, Long idFuncao, List<VwDerAll> vwDerAllList) {
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

                vwDerAll.setFuncaoId(idFuncao);
                vwDerAll.setNome(item.getNome());
                if(!vwDerAllList.contains(vwDerAll)){
                    vwDerAlls.add(vwDerAll);
                }

                vwDer.setNome(item.getNome());
                vwDer.setIdSistemaFD(idSistema);
                if(!vwDerList.contains(vwDer)){
                    vwDers.add(vwDer);
                }
            });
            if(!vwDerAlls.isEmpty()){
                vwDerAllSearchRepository.save(vwDerAlls);
            }
            if(!vwDers.isEmpty()){
                vwDerSearchRepository.save(vwDers);
            }
        }
    }
}
