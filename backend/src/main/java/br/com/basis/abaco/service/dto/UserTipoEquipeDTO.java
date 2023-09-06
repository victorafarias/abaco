package br.com.basis.abaco.service.dto;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Id;
import java.io.Serializable;

@Getter
@Setter
public class UserTipoEquipeDTO implements Serializable {

    @Id
    private Long id;

    private String firstName;

    private String lastName;


}
