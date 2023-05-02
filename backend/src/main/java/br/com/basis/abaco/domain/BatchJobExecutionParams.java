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
@Getter
@Setter
@Table(name = "batch_job_execution_params")
@EqualsAndHashCode
public class BatchJobExecutionParams {

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

    public Timestamp getDateVal() {
        return ObjectUtils.clone(dateVal);
    }

    public void setDateVal(Timestamp dateVal) {
        this.dateVal = ObjectUtils.clone(dateVal);
    }
}
