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
@Table(name = "batch_step_execution")
public class BatchStepExecution {

    @Id
    @NotNull
    @Column(name = "step_execution_id")
    private Long id;

    @NotNull
    @Column(name = "version")
    private Long version;

    @NotNull
    @Column(name = "step_name")
    private String step_name;

    @NotNull
    @OneToOne
    @JoinColumn(name = "job_execution_id")
    private BatchJobExecution batchJobExecution;

    @NotNull
    @Column(name = "start_time")
    private Timestamp start_time;

    @Column(name = "end_time")
    private Timestamp end_time;

    @Column(name = "status")
    private String status;

    @Column(name = "commit_count")
    private Long commit_count;

    @Column(name = "read_count")
    private Long read_count;

    @Column(name = "filter_count")
    private Long filter_count;

    @Column(name = "write_count")
    private Long write_count;

    @Column(name = "read_skip_count")
    private Long read_skip_count;

    @Column(name = "write_skip_count")
    private Long write_skip_count;

    @Column(name = "process_skip_count")
    private Long process_skip_count;

    @Column(name = "rollback_count")
    private Long rollback_count;

    @Column(name = "exit_code")
    private String exit_code;

    @Column(name = "exit_message")
    private String exit_message;

    @Column(name = "last_updated")
    private Timestamp last_updated;

}
