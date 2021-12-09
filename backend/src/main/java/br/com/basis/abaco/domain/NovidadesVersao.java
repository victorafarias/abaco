package br.com.basis.abaco.domain;

import br.com.basis.dynamicexports.pojo.ReportObject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "novidades_versao")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NoArgsConstructor
@AllArgsConstructor
public class NovidadesVersao implements Serializable, ReportObject {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "versao")
    private String versao;

    @OneToMany(mappedBy = "novidadesVersao")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<Novidades> novidades = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersao() {
        return versao;
    }

    public void setVersao(String versao) {
        this.versao = versao;
    }

    public Set<Novidades> getNovidades() {
        return novidades;
    }

    public void setNovidades(Set<Novidades> novidades) {
        this.novidades = novidades;
    }
}
