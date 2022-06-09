package br.com.basis.abaco.web.rest;

import br.com.basis.abaco.domain.Configuracao;
import br.com.basis.abaco.domain.VwAlr;
import br.com.basis.abaco.domain.VwAlrAll;
import br.com.basis.abaco.domain.VwDer;
import br.com.basis.abaco.domain.VwDerAll;
import br.com.basis.abaco.domain.VwRlr;
import br.com.basis.abaco.domain.VwRlrAll;
import br.com.basis.abaco.repository.ConfiguracaoRepository;
import br.com.basis.abaco.utils.ConfiguracaoUtils;
import com.codahale.metrics.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ConfiguracaoResource {

    @Autowired
    private ConfiguracaoRepository configuracaoRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @GetMapping("/configuracao")
    @Timed
    @Secured("ROLE_ABACO_CONFIGURACAO_EDITAR")
    public ResponseEntity<Configuracao> buscarConfiguracao() {
        Configuracao configuracao = configuracaoRepository.findAll().stream().findFirst().orElse(new Configuracao());
        ConfiguracaoUtils configuracaoUtils = ConfiguracaoUtils.getInstance();
        configuracaoUtils.setHabilitarCamposFuncao(configuracao.getHabilitarCamposFuncao());
        return new ResponseEntity<>(configuracao, HttpStatus.OK);
    }

    @PatchMapping("/configuracao")
    @Secured("ROLE_ABACO_CONFIGURACAO_EDITAR")
    public void salvarConfiguracao(@RequestBody Configuracao configuracao){
        configuracao = configuracaoRepository.save(configuracao);
        ConfiguracaoUtils configuracaoUtils = ConfiguracaoUtils.getInstance();
        configuracaoUtils.setHabilitarCamposFuncao(configuracao.getHabilitarCamposFuncao());

        if(configuracao.getHabilitarCamposFuncao() == false){
            elasticsearchTemplate.deleteIndex(VwDer.class);
            elasticsearchTemplate.deleteIndex(VwAlr.class);
            elasticsearchTemplate.deleteIndex(VwRlr.class);
            elasticsearchTemplate.deleteIndex(VwDerAll.class);
            elasticsearchTemplate.deleteIndex(VwAlrAll.class);
            elasticsearchTemplate.deleteIndex(VwRlrAll.class);
        }
    }
}
