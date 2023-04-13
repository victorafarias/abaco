package br.com.basis.abaco.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@Table(name = "batch_job_instance")
public class BatchJobInstance {

    @Id
    @NotNull
    @Column(name = "job_instance_id")
    @JoinColumn(name = "job_instance_id")
    private Long id;

    @Column(name = "version")
    private String version;

    @Column(name = "job_name")
    private String job_name;

    @Column(name = "job_key")
    private String job_key;
}
