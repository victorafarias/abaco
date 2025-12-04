package br.com.basis.abaco.web.rest;

import br.com.basis.abaco.domain.Alr;
import br.com.basis.abaco.domain.Analise;
import br.com.basis.abaco.domain.Der;
import br.com.basis.abaco.domain.FuncaoTransacao;
import br.com.basis.abaco.domain.UploadedFile;
import br.com.basis.abaco.domain.enumeration.Complexidade;
import br.com.basis.abaco.domain.enumeration.MetodoContagem;
import br.com.basis.abaco.domain.enumeration.StatusFuncao;
import br.com.basis.abaco.repository.AnaliseRepository;
import br.com.basis.abaco.repository.DerRepository;
import br.com.basis.abaco.repository.FuncaoTransacaoRepository;
import br.com.basis.abaco.repository.search.FuncaoTransacaoSearchRepository;
import br.com.basis.abaco.repository.search.VwAlrSearchRepository;
import br.com.basis.abaco.repository.search.VwDerSearchRepository;
import br.com.basis.abaco.service.AnaliseService;
import br.com.basis.abaco.service.ConfiguracaoService;
import br.com.basis.abaco.service.FuncaoDadosService;
import br.com.basis.abaco.service.FuncaoTransacaoService;
import br.com.basis.abaco.service.dto.AlrDTO;
import br.com.basis.abaco.service.dto.DerFtDTO;
//import br.com.basis.abaco.service.dto.DerDTO;
import br.com.basis.abaco.service.dto.FuncaoImportarDTO;
import br.com.basis.abaco.service.dto.FuncaoOrdemDTO;
import br.com.basis.abaco.service.dto.FuncaoPFDTO;
import br.com.basis.abaco.service.dto.FuncaoTransacaoAnaliseDTO;
import br.com.basis.abaco.service.dto.FuncaoTransacaoApiDTO;
import br.com.basis.abaco.service.dto.FuncaoTransacaoDTO;
import br.com.basis.abaco.service.dto.FuncaoTransacaoSaveDTO;
import br.com.basis.abaco.service.dto.ImportarFTDTO;
import br.com.basis.abaco.web.rest.util.HeaderUtil;
import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * REST controller for managing FuncaoTransacao.
 */
@RestController
@RequestMapping("/api")
public class FuncaoTransacaoResource {
    private final Logger log = LoggerFactory.getLogger(FuncaoTransacaoResource.class);
    private static final String ENTITY_NAME = "funcaoTransacao";
    private final FuncaoTransacaoRepository funcaoTransacaoRepository;
    private final FuncaoTransacaoSearchRepository funcaoTransacaoSearchRepository;
    private final AnaliseRepository analiseRepository;
    private final VwDerSearchRepository vwDerSearchRepository;
    private final VwAlrSearchRepository vwAlrSearchRepository;
    private final FuncaoDadosService funcaoDadosService;
    @Autowired
    private DerRepository derRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private AnaliseService analiseService;

    @Autowired
    private FuncaoTransacaoService funcaoTransacaoService;


    @Autowired
    private ConfiguracaoService configuracaoService;

    public FuncaoTransacaoResource(FuncaoTransacaoRepository funcaoTransacaoRepository, FuncaoTransacaoSearchRepository funcaoTransacaoSearchRepository, AnaliseRepository analiseRepository, VwDerSearchRepository vwDerSearchRepository, VwAlrSearchRepository vwAlrSearchRepository, FuncaoDadosService funcaoDadosService) {
        this.funcaoTransacaoRepository = funcaoTransacaoRepository;
        this.funcaoTransacaoSearchRepository = funcaoTransacaoSearchRepository;
        this.analiseRepository = analiseRepository;
        this.vwDerSearchRepository = vwDerSearchRepository;
        this.vwAlrSearchRepository = vwAlrSearchRepository;
        this.funcaoDadosService = funcaoDadosService;
    }

    /**
     * POST  /funcaoTransacao : Create a new funcaoTransacao.
     *
     * @param funcaoTransacaoSaveDTO the funcaoTransacao to create
     * @return the ResponseEntity with status 201 (Created) and with body the new funcaoTransacao, or with status 400 (Bad Request) if the funcaoTransacao has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping(path = "/funcaoTransacao/{idAnalise}", consumes = {"multipart/form-data"})
    @Timed
    public ResponseEntity<FuncaoTransacao> createFuncaoTransacao(@PathVariable Long idAnalise, @RequestPart("funcaoTransacao") FuncaoTransacaoSaveDTO funcaoTransacaoSaveDTO, @RequestPart("files")List<MultipartFile> files) throws URISyntaxException {

        FuncaoTransacao funcaoTransacao = convertToEntity(funcaoTransacaoSaveDTO);

        log.debug("REST request to save FuncaoTransacao : {}", funcaoTransacao);
        Analise analise = analiseRepository.findOneByIdClean(idAnalise);
        funcaoTransacao.getDers().forEach(der -> der.setFuncaoTransacao(funcaoTransacao));
        funcaoTransacao.getAlrs().forEach((alr -> alr.setFuncaoTransacao(funcaoTransacao)));
        funcaoTransacao.setAnalise(analise);
        
        //if (funcaoTransacao.getId() != null || analise.getId() == null) {
        //    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new funcaoTransacao cannot already have an ID")).body(null);
        //}

        // Atualizado
        funcaoTransacao.setId(null);

        if(!files.isEmpty()){
            List<UploadedFile> uploadedFiles = funcaoDadosService.uploadFiles(files);
            funcaoTransacao.setFiles(uploadedFiles);
        }
        if(analise.getMetodoContagem().equals(MetodoContagem.ESTIMADA)){
            funcaoTransacao.setComplexidade(Complexidade.MEDIA);
        }

        funcaoTransacao.setDers(bindDers(funcaoTransacao));

        FuncaoTransacao result = funcaoTransacaoRepository.save(funcaoTransacao);

        if(Boolean.TRUE.equals(configuracaoService.buscarConfiguracaoHabilitarCamposFuncao()) && analise.getMetodoContagem().equals(MetodoContagem.DETALHADA)){
            funcaoTransacaoService.saveVwDersAndVwAlrs(result.getDers(), result.getAlrs(), analise.getSistema().getId(), result.getId());
        }

        return ResponseEntity.created(new URI("/api/funcaoTransacao/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT  /funcaoTransacao : Updates an existing funcaoTransacao.
     *
     * @param funcaoTransacaoDTO the funcaoTransacao to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated funcaoTransacao,
     * or with status 400 (Bad Request) if the funcaoTransacao is not valid,
     * or with status 500 (Internal Server Error) if the funcaoTransacao couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping(path = "/funcaoTransacao/{id}", consumes = {"multipart/form-data"})
    @Timed
    public ResponseEntity<FuncaoTransacao> updateFuncaoTransacao(@PathVariable Long id, @RequestPart("funcaoTransacao") FuncaoTransacaoSaveDTO funcaoTransacaoDTO, @RequestPart("files")List<MultipartFile> files) throws URISyntaxException{
        FuncaoTransacao funcaoTransacao = convertToEntity(funcaoTransacaoDTO);

        log.debug("REST request to update FuncaoTransacao : {}", funcaoTransacao);
        FuncaoTransacao funcaoTransacaoOld = funcaoTransacaoRepository.findOne(id);
        Analise analise = analiseRepository.findOneByIdClean(funcaoTransacaoOld.getAnalise().getId());
        funcaoTransacao.getDers().forEach(der -> der.setFuncaoTransacao(funcaoTransacao));
        funcaoTransacao.getAlrs().forEach((alr -> alr.setFuncaoTransacao(funcaoTransacao)));
        funcaoTransacao.setAnalise(analise);

        if (funcaoTransacao.getId() == null) {
            return createFuncaoTransacao(analise.getId(), funcaoTransacaoDTO, files);
        }

        if (funcaoTransacao.getAnalise() == null || funcaoTransacao.getAnalise().getId() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new funcaoTransacao cannot already have an ID")).body(null);
        }
        if(!files.isEmpty()){
            List<UploadedFile> uploadedFiles = funcaoDadosService.uploadFiles(files);
            funcaoTransacao.setFiles(uploadedFiles);
        }
        if(analise.getMetodoContagem().equals(MetodoContagem.ESTIMADA)){
            funcaoTransacao.setComplexidade(Complexidade.MEDIA);
        }

        FuncaoTransacao result = funcaoTransacaoRepository.save(funcaoTransacao);

        if(Boolean.TRUE.equals(configuracaoService.buscarConfiguracaoHabilitarCamposFuncao()) && analise.getMetodoContagem().equals(MetodoContagem.DETALHADA)){
            funcaoTransacaoService.saveVwDersAndVwAlrs(result.getDers(), result.getAlrs(), analise.getSistema().getId(), result.getId());
        }

        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, funcaoTransacao.getId().toString())).body(result);
    }

    /**
     * GET  /funcao-transacao : get all the funcaotransacao.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of funcaotransacao in body
     */
    @GetMapping("/funcao-transacao")
    @Timed
    public List<FuncaoTransacao> getAllFuncaoTransacaos() {
        log.debug("REST request to get all funcaotransacao");
        List<FuncaoTransacao> funcaoTransacao = funcaoTransacaoRepository.findAll();
        funcaoTransacao.stream().filter(f -> f.getAnalise() != null).forEach(f -> {
            if (f.getAnalise().getFuncaoDados() != null) {
                f.getAnalise().getFuncaoDados().clear();
            }
            if (f.getAnalise().getFuncaoTransacao() != null) {
                f.getAnalise().getFuncaoTransacao().clear();
            }
        });
        return funcaoTransacao;
    }

    /**
     * GET  /funcaoTransacao/:id : get the "id" funcaoTransacao.
     *
     * @param id the id of the funcaoTransacao to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the funcaoTransacao, or with status 404 (Not Found)
     */
    @GetMapping("/funcaoTransacao/{id}")
    @Timed
    public ResponseEntity<FuncaoTransacaoApiDTO> getFuncaoTransacao(@PathVariable Long id) {
        log.debug("REST request to get FuncaoTransacao : {}", id);
        FuncaoTransacao funcaoTransacao = funcaoTransacaoRepository.findOne(id);

        FuncaoTransacaoApiDTO funcaoDadosDTO = modelMapper.map(funcaoTransacao, FuncaoTransacaoApiDTO.class);

        Set<DerFtDTO> ders = new LinkedHashSet<>();
        Set<AlrDTO> alrs = new LinkedHashSet<>();
        funcaoTransacao.getDers().forEach(der -> {
            DerFtDTO derDto = new DerFtDTO();
            derDto.setNome(der.getNome());
            derDto.setValor(der.getValor());
            ders.add(derDto);
        });
        funcaoTransacao.getAlrs().forEach(alr -> {
            AlrDTO alrDto = new AlrDTO();
            alrDto.setNome(alr.getNome());
            alrDto.setValor(alr.getValor());
            alrs.add(alrDto);
        });
        funcaoDadosDTO.setDers(ders);
        funcaoDadosDTO.setAlrs(alrs);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(funcaoDadosDTO));
    }

    @GetMapping("/funcaoTransacao/analise/{id}")
    @Timed
    public Set<FuncaoTransacao> getFuncaoTransacaoAnalise(@PathVariable Long id) {
        return funcaoTransacaoRepository.findAllByAnaliseId(id);
    }

    @GetMapping("/funcaoTransacao-dto/analise/{id}")
    @Timed
    public ResponseEntity<List<FuncaoTransacaoAnaliseDTO>> getFuncaoTransacaoByAnalise(@PathVariable Long id) {
        Set<FuncaoTransacao> lstFuncadoTransacao = funcaoTransacaoRepository.findAllByAnaliseId(id);
        List<FuncaoTransacaoAnaliseDTO> lstFuncaoDadosDTO = lstFuncadoTransacao.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(lstFuncaoDadosDTO));
    }

    /**
     * GET  /funcaoTransacao/completa/:id : get the "id" funcaotransacao.
     *
     * @param id the id of the funcaoTransacao to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the funcaoTransacao, or with status 404 (Not Found)
     */
    @GetMapping("/funcaoTransacao/completa/{id}")
    @Timed
    public ResponseEntity<FuncaoTransacaoApiDTO> getFuncaoTransacaoCompleta(@PathVariable Long id) {
        log.debug("REST request to get FuncaoTransacao Completa : {}", id);
        FuncaoTransacao funcaoTransacao = funcaoTransacaoRepository.findWithDerAndAlr(id);
        FuncaoTransacaoApiDTO funcaoDadosDTO = modelMapper.map(funcaoTransacao, FuncaoTransacaoApiDTO.class);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(funcaoDadosDTO));
    }

    /**
     * DELETE  /funcaoTransacao/:id : delete the "id" funcaoTransacao.
     *
     * @param id the id of the funcaoTransacao to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/funcaoTransacao/{id}")
    @Timed
    public ResponseEntity<Void> deleteFuncaoTransacao(@PathVariable Long id) {
        log.debug("REST request to delete FuncaoTransacao : {}", id);
        FuncaoTransacao funcaoTransacao = funcaoTransacaoRepository.findOne(id);
        if(Boolean.TRUE.equals(configuracaoService.buscarConfiguracaoHabilitarCamposFuncao())){
            funcaoTransacao.getDers().forEach(item -> vwDerSearchRepository.delete(item.getId()));
            funcaoTransacao.getAlrs().forEach(item -> vwAlrSearchRepository.delete(item.getId()));
        }
        funcaoTransacaoRepository.delete(id);
        funcaoTransacaoSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/funcaoTransacao?query=:query : search for the funcaoTransacao corresponding
     * to the query.
     *
     * @param query the query of the funcaoTransacao search
     * @return the result of the search
     */
    @GetMapping("/_search/funcaoTransacao")
    @Timed
    public List<FuncaoTransacao> searchFuncaoTransacaos(@RequestParam(defaultValue = "*") String query) {
        log.debug("REST request to search funcaotransacao for query {}", query);
        return StreamSupport
                .stream(funcaoTransacaoSearchRepository.search(queryStringQuery(query)).spliterator(), false)
                .collect(Collectors.toList());
    }

    @GetMapping("/funcaoTransacao/{idAnalise}/{idfuncionalidade}")
    @Timed
    public ResponseEntity<Boolean> existFuncaoDados(@PathVariable Long idAnalise, @PathVariable Long idfuncionalidade, @RequestParam String name, @RequestParam(required = false) Long id) {
        log.debug("REST request to exist FuncaoDados");
        Boolean existInAnalise;
        if (id != null && id > 0) {

            existInAnalise = funcaoTransacaoRepository.existsByNameAndAnaliseIdAndFuncionalidadeIdAndIdNot(name, idAnalise, idfuncionalidade, id);
        } else {
            existInAnalise = funcaoTransacaoRepository.existsByNameAndAnaliseIdAndFuncionalidadeId(name, idAnalise, idfuncionalidade);
        }
        return ResponseEntity.ok(existInAnalise);
    }
    @GetMapping("/funcaoTransacao/divergencia/{idAnalise}/{idfuncionalidade}")
    @Timed
    public ResponseEntity<Boolean> existFuncaoDadosDivergencia(@PathVariable Long idAnalise, @PathVariable Long idfuncionalidade, @RequestParam String name, @RequestParam(required = false) Long id, @RequestParam(required = false)Long idEquipe) {
        log.debug("REST request to exist FuncaoDados");
        Boolean existInAnalise = false;
        if(idEquipe != null && idEquipe > 0){
            if (id != null && id > 0) {
                existInAnalise = funcaoTransacaoRepository.existsByNameAndAnaliseIdAndFuncionalidadeIdAndIdNotAndEquipeId(name, idAnalise, idfuncionalidade, id, idEquipe);
            } else {
                existInAnalise = funcaoTransacaoRepository.existsByNameAndAnaliseIdAndFuncionalidadeIdAndEquipeId(name, idAnalise, idfuncionalidade, idEquipe);
            }
        }else{
            if (id != null && id > 0) {
                existInAnalise = funcaoTransacaoRepository.existsByNameAndAnaliseIdAndFuncionalidadeIdAndIdNot(name, idAnalise, idfuncionalidade, id);
            } else {
                existInAnalise = funcaoTransacaoRepository.existsByNameAndAnaliseIdAndFuncionalidadeId(name, idAnalise, idfuncionalidade);
            }
        }
        return ResponseEntity.ok(existInAnalise);
    }
    /**
     * GET  /funcaoTransacao/:id : get the "id" funcaoTransacao.
     *
     * @param id the id of the funcaoTransacao to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the funcaoTransacao, or with status 404 (Not Found)
     */
    @GetMapping("/funcaoTransacao/update-status/{id}/{statusFuncao}")
    @Timed
    public ResponseEntity<FuncaoTransacaoDTO> updateFuncaoTransacao(@PathVariable Long id, @PathVariable StatusFuncao statusFuncao) {
        log.debug("REST request to get FuncaoTransacao by Status : {}", id);
        FuncaoTransacao funcaoTransacao = funcaoTransacaoRepository.findOne(id);
        funcaoTransacao.setStatusFuncao(statusFuncao);
        funcaoTransacaoRepository.save(funcaoTransacao);
        analiseService.salvar(funcaoTransacao.getAnalise());
        FuncaoTransacaoDTO funcaoDadosDTO = modelMapper.map(funcaoTransacao, FuncaoTransacaoDTO.class);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(funcaoDadosDTO));
    }

    @PatchMapping("/funcaoTransacao/update-ordem")
    public ResponseEntity<Void> updateOrdemFuncao(@RequestBody FuncaoOrdemDTO funcaoOrdemDTO){
        if(funcaoOrdemDTO != null){
            log.debug("REST request to update ordem FUNCAO T: {}", funcaoOrdemDTO.getId());
            FuncaoTransacao funcaoTransacao = funcaoTransacaoRepository.findOne(funcaoOrdemDTO.getId());
            funcaoTransacao.setOrdem(funcaoOrdemDTO.getOrdem());
            funcaoTransacaoRepository.save(funcaoTransacao);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(path = "/funcaoTransacao/importar-funcoes-analise", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ImportarFTDTO> importarFuncaoAnalise(@RequestBody FuncaoImportarDTO funcaoImportarDTO)  {
        return new ResponseEntity<>(funcaoTransacaoService.importarFuncaoAnalise(funcaoImportarDTO), HttpStatus.OK);
    }

    @PatchMapping("/funcaoTransacao/update-pf")
    @Timed
    public ResponseEntity<Void> updatePF(@RequestBody List<FuncaoPFDTO> funcaoPFDTO){
        log.debug("REST request to update PF de Transação em lote");
        
        if(funcaoPFDTO != null && !funcaoPFDTO.isEmpty()){
            // Delega para o serviço refatorado
            funcaoTransacaoService.updatePF(funcaoPFDTO);
        }
        
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private FuncaoTransacaoAnaliseDTO convertToDto(FuncaoTransacao funcaoTransacao) {
        FuncaoTransacaoAnaliseDTO funcaoTransacaoAnaliseDTO = modelMapper.map(funcaoTransacao, FuncaoTransacaoAnaliseDTO.class);
        funcaoTransacaoAnaliseDTO.setFtrFilter(getValueFtr(funcaoTransacao));
        funcaoTransacaoAnaliseDTO.setDerFilter(getValueDer(funcaoTransacao));
        funcaoTransacaoAnaliseDTO.setHasSustantation(getSustantation(funcaoTransacao));
        return funcaoTransacaoAnaliseDTO;
    }


    private Integer getValueDer(FuncaoTransacao funcaoTransacao) {
        int dersValues = funcaoTransacao.getDers().size();
        if (dersValues == 1) {
            Der der = funcaoTransacao.getDers().iterator().next();
            if (der.getValor() != null) {
                dersValues = der.getValor();
            }
        }
        return dersValues;
    }

    private Integer getValueFtr(FuncaoTransacao funcaoTransacao) {
        int alrValues = funcaoTransacao.getAlrs().size();
        if (alrValues >= 1) {
            Alr alr = funcaoTransacao.getAlrs().iterator().next();
            if (alr.getValor() != null) {
                alrValues = alr.getValor();
            }
        }
        return alrValues;
    }

    private Boolean getSustantation(FuncaoTransacao funcaoTransacao) {
        return funcaoTransacao.getSustantation() != null && !(funcaoTransacao.getSustantation().isEmpty());
    }


    @NotNull
    private Set<Der> bindDers(@RequestBody FuncaoTransacao funcaoTransacao) {
        Set<Der> ders = new LinkedHashSet<>();
        funcaoTransacao.getDers().forEach(der -> {
            if (der.getId() != null) {
                der = derRepository.findOne(der.getId());
                der = new Der(null, der.getNome(), der.getValor(), der.getRlr(), null, funcaoTransacao);
                ders.add(der);
            } else {
                ders.add(der);
            }
        });
        return ders;
    }

    private FuncaoTransacao convertToEntity(FuncaoTransacaoSaveDTO funcaoTransacaoSaveDTO){
        Set<Der> ders = new LinkedHashSet<>();
        Set<Alr> alrs = new LinkedHashSet<>();
        FuncaoTransacao map = modelMapper.map(funcaoTransacaoSaveDTO, FuncaoTransacao.class);
        funcaoTransacaoSaveDTO.getDers().forEach(derDto -> {
            Der der = new Der();
            der.setNome(derDto.getNome());
            der.setValor(derDto.getValor());
            ders.add(der);
        });
        funcaoTransacaoSaveDTO.getAlrs().forEach(alrDto -> {
            Alr alr = new Alr();
            alr.setNome(alrDto.getNome());
            alr.setValor(alrDto.getValor());
            alrs.add(alr);
        });
        map.setDers(ders);
        map.setAlrs(alrs);
        return map;
    }



}
