package br.com.basis.abaco.service.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author eduardo.andrade
 * @since 29/06/2018
 */
@Getter
@Setter
@EqualsAndHashCode
public class AlrDTO implements Comparable<AlrDTO>, Serializable {
    private Long id;
    private String nome;
    private Integer valor;
    private Integer numeracao;

    @Override
    public int compareTo(@NotNull AlrDTO alrDTO) {
        if((alrDTO).getNumeracao() != null && numeracao != null){
            return numeracao - (alrDTO).getNumeracao();
        }else{
            return 1;
        }
    }
}
