package br.com.basis.abaco.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class CompartilhadaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long equipeId;

    private List<Long> analisesId;

    private boolean viewOnly;

    private String nomeEquipe;
}
