package br.com.basis.abaco.domain;

import br.com.basis.abaco.service.dto.ConfiguracaoDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * A Configuracao.
 */
@Entity
@Table(name = "configuracao")
@Getter @Setter @NoArgsConstructor
public class Configuracao implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;


    @Column(name = "habilitar_campos_funcao")
    private Boolean habilitarCamposFuncao;

    public Configuracao(ConfiguracaoDTO configuracaoDTO){
        this.id = configuracaoDTO.getId();
        this.habilitarCamposFuncao = configuracaoDTO.getHabilitarCamposFuncao();
    }

}
