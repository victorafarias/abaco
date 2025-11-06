package br.com.basis.abaco.domain;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ObjectUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@EqualsAndHashCode
@Table(name = "batch_job_execution_params")
public class BatchJobExecutionParams implements Serializable {

    @Id
    @NotNull
    @Column(name = "job_execution_id")
    private Long id;

    @NotNull
    @Column(name = "type_cd")
    private String typeCd;

    @NotNull
    @Column(name = "key_name")
    private String keyName;

    @Column(name = "string_val")
    private String stringVal;

    @Column(name = "date_val")
    private Timestamp dateVal;

    @Column(name = "long_val")
    private Long longVal;

    @Column(name = "double_val")
    private Double doubleVal;

    @NotNull
    @Column(name = "identifying")
    private String identifying;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTypeCd() {
        return typeCd;
    }

    public void setTypeCd(String typeCd) {
        this.typeCd = typeCd;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getStringVal() {
        return stringVal;
    }

    public void setStringVal(String stringVal) {
        this.stringVal = stringVal;
    }

    public Timestamp getDateVal() {
        return ObjectUtils.clone(dateVal);
    }

    public void setDateVal(Timestamp dateVal) {
        this.dateVal = ObjectUtils.clone(dateVal);
    }

    public Long getLongVal() {
        return longVal;
    }

    public void setLongVal(Long longVal) {
        this.longVal = longVal;
    }

    public Double getDoubleVal() {
        return doubleVal;
    }

    public void setDoubleVal(Double doubleVal) {
        this.doubleVal = doubleVal;
    }

    public String getIdentifying() {
        return identifying;
    }

    public void setIdentifying(String identifying) {
        this.identifying = identifying;
    }
}
