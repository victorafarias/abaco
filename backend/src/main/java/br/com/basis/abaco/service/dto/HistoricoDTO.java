package br.com.basis.abaco.service.dto;

import br.com.basis.dynamicexports.pojo.ReportObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoDTO implements ReportObject, Serializable {

    private Long id;

    private AnaliseDTO analise;

    private Timestamp dtAcao;

    private UserDTO usuario;

    private String acao;

    public void setDtAcao(Timestamp dtAcao) {
        if (dtAcao != null) {
            this.dtAcao = new Timestamp(dtAcao.getTime());
        } else {
            this.dtAcao = null;
        }
    }

    public Timestamp getDtAcao() {
        return this.dtAcao != null ? new Timestamp(this.dtAcao.getTime()) : null;
    }
}
