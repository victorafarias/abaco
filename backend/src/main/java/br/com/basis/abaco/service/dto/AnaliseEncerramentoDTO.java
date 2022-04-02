package br.com.basis.abaco.service.dto;

import br.com.basis.abaco.domain.Compartilhada;
import br.com.basis.abaco.domain.enumeration.MetodoContagem;
import br.com.basis.abaco.domain.enumeration.TipoAnalise;
import br.com.basis.dynamicexports.pojo.ReportObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class AnaliseEncerramentoDTO implements ReportObject, Serializable {

    private Long id;
    private boolean isEncerrada;
    private Timestamp dtEncerramento;


    public void setDtEncerramento(Timestamp dtEncerramento) {
        this.dtEncerramento = dtEncerramento == null ? null : new Timestamp(dtEncerramento.getTime());
    }

    public Timestamp getDtEncerramento() {
        return dtEncerramento == null ? null : new Timestamp(this.dtEncerramento.getTime());
    }
}
