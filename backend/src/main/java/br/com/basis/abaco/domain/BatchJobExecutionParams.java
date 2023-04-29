package br.com.basis.abaco.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Entity
@Table(name = "batch_job_execution_params")
@EqualsAndHashCode
public class BatchJobExecutionParams {

    @Id
    @NotNull
    @Column(name = "job_execution_id")
    private Long id;

    @NotNull
    @Column(name = "type_cd")
    private String type_cd;

    @NotNull
    @Column(name = "key_name")
    private String key_name;

    @Column(name = "string_val")
    private String string_val;

    @Column(name = "date_val")
    private Timestamp date_val;

    @Column(name = "long_val")
    private Long long_val;

    @Column(name = "double_val")
    private Double double_val;

    @NotNull
    @Column(name = "identifying")
    private String identifying;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType_cd() {
        return type_cd;
    }

    public void setType_cd(String type_cd) {
        this.type_cd = type_cd;
    }

    public String getKey_name() {
        return key_name;
    }

    public void setKey_name(String key_name) {
        this.key_name = key_name;
    }

    public String getString_val() {
        return string_val;
    }

    public void setString_val(String string_val) {
        this.string_val = string_val;
    }

    public Timestamp getDate_val() {
        return ObjectUtils.clone(date_val);
    }

    public void setDate_val(Timestamp date_val) {
        this.date_val = ObjectUtils.clone(date_val);
    }

    public Long getLong_val() {
        return long_val;
    }

    public void setLong_val(Long long_val) {
        this.long_val = long_val;
    }

    public Double getDouble_val() {
        return double_val;
    }

    public void setDouble_val(Double double_val) {
        this.double_val = double_val;
    }

    public String getIdentifying() {
        return identifying;
    }

    public void setIdentifying(String identifying) {
        this.identifying = identifying;
    }
}
