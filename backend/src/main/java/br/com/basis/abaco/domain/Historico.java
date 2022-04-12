package br.com.basis.abaco.domain;

import br.com.basis.dynamicexports.pojo.ReportObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Entidade para representar o Histórico.
 */
@Entity
@Table(name = "historico")
@Getter
@Setter
@NoArgsConstructor
public class Historico implements Serializable, ReportObject {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "analise_id")
    private Analise analise;

    @JsonInclude
    @Column(name = "dt_acao")
    private Timestamp dtAcao;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private User usuario;

    @Column(name = "acao")
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

    public String getSomenteData(){
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        return format.format(dtAcao);
    }


    public String getSomenteHora(){
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(dtAcao);
    }
}





