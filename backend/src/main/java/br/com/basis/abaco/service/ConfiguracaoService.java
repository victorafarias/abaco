package br.com.basis.abaco.service;

import br.com.basis.abaco.domain.Configuracao;
import br.com.basis.abaco.repository.ConfiguracaoRepository;
import br.com.basis.abaco.utils.ConfiguracaoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConfiguracaoService {

    @Autowired
    private ConfiguracaoRepository configuracaoRepository;

    public Configuracao criarConfiguracaoPadrao() {
        Configuracao config = new Configuracao();
        config.setHabilitarCamposFuncao(false);
        return config;
    }

    public Boolean buscarConfiguracaoHabilitarCamposFuncao(){
        ConfiguracaoUtils configuracaoUtils = ConfiguracaoUtils.getInstance();
        if(configuracaoUtils.getHabilitarCamposFuncao() == null){
            configuracaoUtils.setHabilitarCamposFuncao(configuracaoRepository.findAll().stream().findFirst().orElse(criarConfiguracaoPadrao()).getHabilitarCamposFuncao());
        }

        return configuracaoUtils.getHabilitarCamposFuncao();
    }
}
