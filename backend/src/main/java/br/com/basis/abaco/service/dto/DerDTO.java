package br.com.basis.abaco.service.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class DerDTO implements Comparable<DerDTO>, Serializable {

    private Long id;

    private String nome;

    private Integer valor;

    private RlrDTO rlr;

    private Integer numeracao;

    @Override
    public int compareTo(@NotNull DerDTO derDTO) {
        if(derDTO.getNumeracao() != null && numeracao != null){
            return numeracao - derDTO.getNumeracao();
        }else{
            return 1;
        }
    }
}
