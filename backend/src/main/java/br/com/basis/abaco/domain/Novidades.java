package br.com.basis.abaco.domain;

import br.com.basis.dynamicexports.pojo.ReportObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "novidades")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NoArgsConstructor
@AllArgsConstructor
public class Novidades implements Serializable, ReportObject {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "ocorrencia")
    private String ocorrencia;

    @Lob @Basic(fetch = FetchType.LAZY)
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "novidades")
    private String novidade;

    @ManyToOne
    @JoinColumn(name = "novidades_versao_id")
    @JsonIgnore
    private NovidadesVersao novidadesVersao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOcorrencia() {
        return ocorrencia;
    }

    public void setOcorrencia(String ocorrencia) {
        this.ocorrencia = ocorrencia;
    }

    public String getNovidade() {
        return novidade;
    }

    public void setNovidade(String novidade) {
        this.novidade = novidade;
    }

    public NovidadesVersao getNovidadesVersao() {
        return novidadesVersao;
    }

    public void setNovidadesVersao(NovidadesVersao novidadesVersao) {
        this.novidadesVersao = novidadesVersao;
    }
}
