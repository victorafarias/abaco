package br.com.basis.abaco.service.dto;

import br.com.basis.abaco.domain.enumeration.Complexidade;
import br.com.basis.abaco.domain.enumeration.StatusFuncao;
import br.com.basis.abaco.domain.enumeration.TipoFuncaoTransacao;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * @author eduardo.andrade
 * @since 28/06/2018
 */
@Getter
@Setter
@NoArgsConstructor
public class FuncaoTransacaoDTO implements Serializable {

    private Long id;
    private String name;
    private String nomeFt;
    private BigDecimal pf;
    private BigDecimal grossPF;
    private String classificacaoFt;
    private String impactoFt;
    private String ftrFt;
    private String derFt;
    private String complexidadeFt;
    private String pfTotalFt;
    private String pfAjustadoFt;
    private String fatorAjusteFt;
    private String fatorAjusteValor;
    private String modulo;
    private String submodulo;
    private Integer identificador;
    private String sustantation;
    private String der;
    private String ftr;
    private StatusFuncao statusFuncao;
    private Complexidade complexidade;
    private TipoFuncaoTransacao tipo;
    private FuncionalidadeDTO funcionalidade;
    private FatorAjusteDTO fatorAjuste;

    private Set<AlrDTO> alrs = new HashSet<>();
    private Set<DerDTO> ders = new HashSet<>();

}
