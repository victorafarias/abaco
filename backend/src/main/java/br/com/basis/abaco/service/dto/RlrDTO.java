package br.com.basis.abaco.service.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@EqualsAndHashCode
public class RlrDTO implements Comparable<RlrDTO>, Serializable {

    private Long id;

    private String nome;

    private Integer valor;

    private Integer numeracao;

    @Override
    public int compareTo(@NotNull RlrDTO rlrDTO) {
        if(rlrDTO.getNumeracao() != null && numeracao != null){
            return numeracao - rlrDTO.getNumeracao();
        }
        else{
            return 1;
        }
    }
}
