package br.com.basis.abaco.service.dto.pesquisa;

import br.com.basis.abaco.domain.enumeration.MetodoContagem;
import br.com.basis.abaco.domain.enumeration.TipoDeDataAnalise;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
public class AnalisePesquisaDTO implements Serializable {

    private String order = "ASC";
    private int pageNumber = 0;
    private int size = 20;
    private String sort = "id";
    private String identificador;
    private Set<Long> sistema = new HashSet<>();
    private Set<MetodoContagem> metodo = new HashSet<>();
    private Set<Long> organizacao = new HashSet<>();
    private Long equipe;
    private Set<Long> status = new HashSet<>();
    private Set<Long> usuario = new HashSet<>();
    private TipoDeDataAnalise data;
    private Date dataInicio;
    private Date dataFim;
    private Boolean bloqueado;

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public Set<Long> getSistema() {
        return Collections.unmodifiableSet(sistema);
    }

    public void setSistema(Set<Long> sistema) {
        if (sistema == null) {
            sistema = new HashSet<>();
        }
        this.sistema = Collections.unmodifiableSet(sistema);
    }

    public Set<MetodoContagem> getMetodo() {
        return Collections.unmodifiableSet(metodo);
    }

    public void setMetodo(Set<MetodoContagem> metodo) {
        if (metodo == null) {
            metodo = new HashSet<>();
        }
        this.metodo = Collections.unmodifiableSet(metodo);
    }

    public Set<Long> getOrganizacao() {
        return Collections.unmodifiableSet(organizacao);
    }

    public void setOrganizacao(Set<Long> organizacao) {
        if (organizacao == null) {
            organizacao = new HashSet<>();
        }
        this.organizacao = Collections.unmodifiableSet(organizacao);
    }

    public Long getEquipe() {
        return equipe;
    }

    public void setEquipe(Long equipe) {
        this.equipe = equipe;
    }

    public Set<Long> getStatus() {
        return Collections.unmodifiableSet(status);
    }

    public void setStatus(Set<Long> status) {
        if (status == null) {
            status = new HashSet<>();
        }
        this.status = Collections.unmodifiableSet(status);
    }

    public Set<Long> getUsuario() {
        return Collections.unmodifiableSet(usuario);
    }

    public void setUsuario(Set<Long> usuario) {
        if (usuario == null) {
            usuario = new HashSet<>();
        }
        this.usuario = Collections.unmodifiableSet(usuario);
    }

    public TipoDeDataAnalise getData() {
        return data;
    }

    public void setData(TipoDeDataAnalise data) {
        this.data = ObjectUtils.clone(data);
    }

    public Date getDataInicio() {
        return ObjectUtils.clone(dataInicio);
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = ObjectUtils.clone(dataInicio);
    }

    public Date getDataFim() {
        return ObjectUtils.clone(dataFim);
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = ObjectUtils.clone(dataFim);
    }

    public Boolean getBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(Boolean bloqueado) {
        this.bloqueado = bloqueado;
    }
}
