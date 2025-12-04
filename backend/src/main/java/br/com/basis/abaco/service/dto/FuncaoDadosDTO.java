package br.com.basis.abaco.service.dto;

import br.com.basis.abaco.domain.enumeration.Complexidade;
import br.com.basis.abaco.domain.enumeration.StatusFuncao;
import br.com.basis.abaco.domain.enumeration.TipoFuncaoDados;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author eduardo.andrade
 * @since 28/06/2018
 */
@Getter
@Setter
@NoArgsConstructor
public class FuncaoDadosDTO implements Serializable {

    private Long id;
    private BigDecimal pf;
    private BigDecimal grossPF;
    private String nomeFd;
    private String classificacaoFd;
    private String name;
    private String impactoFd;
    private String rlrFd;
    private String derFd;
    private String complexidadeFd;
    private String pfTotalFd;
    private String pfAjustadoFd;
    private String fatorAjusteFd;
    private String fatorAjusteValor;
    private String modulo;
    private String submodulo;
    private Integer identificador;
    private Long ordem; // Alterado: Campo adicionado para preservar ordem durante importação Excel
    private String sustantation;
    private String der;
    private String rlr;
    private TipoFuncaoDados tipo;
    private StatusFuncao statusFuncao;
    private Complexidade complexidade;
    private FuncionalidadeDTO funcionalidade;
    private FatorAjusteDTO fatorAjuste;
    private List<UploadedFileDTO> files;
    private TipoEquipeDTO equipe;

    private AlrDTO alr;
    private Set<DerDTO> ders = new HashSet<>();
    private Set<RlrDTO> rlrs = new HashSet<>();

}
