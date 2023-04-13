package br.com.basis.abaco.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@Table(name = "batch_job_execution_context")
public class BatchJobExecutionContext {

    @Id
    @NotNull
    @Column(name = "job_execution_id")
    private Long id;

    @NotNull
    @Column(name = "short_context")
    private String short_context;

    @Column(name = "serialized_context")
    private String serialized_context;
}
