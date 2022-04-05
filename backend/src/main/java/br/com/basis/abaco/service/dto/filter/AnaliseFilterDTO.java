package br.com.basis.abaco.service.dto.filter;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import br.com.basis.abaco.domain.enumeration.MetodoContagem;
import br.com.basis.abaco.domain.enumeration.TipoDeDataAnalise;
import br.com.basis.abaco.service.dto.OrganizacaoDTO;
import br.com.basis.abaco.service.dto.SistemaDTO;
import br.com.basis.abaco.service.dto.StatusDTO;
import br.com.basis.abaco.service.dto.TipoEquipeDTO;
import br.com.basis.abaco.service.dto.UserDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnaliseFilterDTO {
    private OrganizacaoDTO organizacao;
    private String identificadorAnalise;
    private TipoEquipeDTO equipe;
    private SistemaDTO sistema;
    private MetodoContagem metodoContagem;
    private UserDTO usuario;
    private StatusDTO status;
    private TipoDeDataAnalise data;
    private Date dataInicio;
    private Date dataFim;
    private List<String> columnsVisible;


    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio == null ? null : new Date(dataInicio.getTime());
    }

    public Date getDataInicio() {
        return dataInicio == null ? null : new Date(this.dataInicio.getTime());
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim == null ? null : new Date(dataFim.getTime());
    }

    public Date getDataFim() {
        return dataFim == null ? null : new Date(this.dataFim.getTime());
    }
}
