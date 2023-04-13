package br.com.basis.abaco.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@Table(name = "batch_job_execution")
public class BatchJobExecution {

    @Id
    @NotNull
    @Column(name = "job_execution_id")
    private Long id;

    @Column(name = "version")
    private Long version;

    @NotNull
    @OneToOne
    @JoinColumn(name = "job_instance_id")
    private BatchJobInstance job_instance_id;

    @NotNull
    @Column(name = "create_time")
    private Timestamp create_time;

    @Column(name = "start_time")
    private Timestamp start_time;

    @Column(name = "end_time")
    private Timestamp end_time;

    @Column(name = "status")
    private String status;

    @Column(name = "exit_code")
    private String exit_code;

    @Column(name = "exit_message")
    private String exit_message;

    @Column(name = "last_updated")
    private Timestamp last_updated;

    @Column(name = "job_configuration_location")
    private String job_configuration_location;

    @Override
    public String toString() {
        return "BatchJobExecution{" +
            "id=" + id +
            ", version=" + version +
            ", job_instance_id=" + job_instance_id +
            ", create_time=" + create_time +
            ", start_time=" + start_time +
            ", end_time=" + end_time +
            ", status='" + status + '\'' +
            ", exit_code='" + exit_code + '\'' +
            ", exit_message='" + exit_message + '\'' +
            ", last_updated=" + last_updated +
            ", job_configuration_location='" + job_configuration_location + '\'' +
            '}';
    }
}
