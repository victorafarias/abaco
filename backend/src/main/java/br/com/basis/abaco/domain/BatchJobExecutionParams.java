package br.com.basis.abaco.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Entity
@Table(name = "batch_job_execution_params")
@Getter
@Setter
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
}
