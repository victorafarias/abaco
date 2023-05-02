package br.com.basis.abaco.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Entity
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
    private String stepName;

    @NotNull
    @OneToOne
    @JoinColumn(name = "job_execution_id")
    private BatchJobExecution batchJobExecution;

    @NotNull
    @Column(name = "start_time")
    private Timestamp startTime;

    @Column(name = "end_time")
    private Timestamp endTime;

    @Column(name = "status")
    private String status;

    @Column(name = "commit_count")
    private Long commitCount;

    @Column(name = "read_count")
    private Long readCount;

    @Column(name = "filter_count")
    private Long filterCount;

    @Column(name = "write_count")
    private Long writeCount;

    @Column(name = "read_skip_count")
    private Long readSkipCount;

    @Column(name = "write_skip_count")
    private Long writeSkipCount;

    @Column(name = "process_skip_count")
    private Long processSkipCount;

    @Column(name = "rollback_count")
    private Long rollbackCount;

    @Column(name = "exit_code")
    private String exitCode;

    @Column(name = "exit_message")
    private String exitMessage;

    @Column(name = "last_updated")
    private Timestamp lastUpdated;


    public Timestamp getStartTime() {
        return ObjectUtils.clone(startTime);
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = ObjectUtils.clone(startTime);
    }

    public Timestamp getEndTime() {
        return ObjectUtils.clone(endTime);
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = ObjectUtils.clone(endTime);
    }

    public Timestamp getLastUpdated() {
        return ObjectUtils.clone(lastUpdated);
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = ObjectUtils.clone(lastUpdated);
    }
}
