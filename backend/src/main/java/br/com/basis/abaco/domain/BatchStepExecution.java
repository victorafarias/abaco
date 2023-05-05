package br.com.basis.abaco.domain;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ObjectUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@EqualsAndHashCode
@Table(name = "batch_step_execution")
public class BatchStepExecution implements Serializable {

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public BatchJobExecution getBatchJobExecution() {
        return batchJobExecution;
    }

    public void setBatchJobExecution(BatchJobExecution batchJobExecution) {
        this.batchJobExecution = batchJobExecution;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCommitCount() {
        return commitCount;
    }

    public void setCommitCount(Long commitCount) {
        this.commitCount = commitCount;
    }

    public Long getReadCount() {
        return readCount;
    }

    public void setReadCount(Long readCount) {
        this.readCount = readCount;
    }

    public Long getFilterCount() {
        return filterCount;
    }

    public void setFilterCount(Long filterCount) {
        this.filterCount = filterCount;
    }

    public Long getWriteCount() {
        return writeCount;
    }

    public void setWriteCount(Long writeCount) {
        this.writeCount = writeCount;
    }

    public Long getReadSkipCount() {
        return readSkipCount;
    }

    public void setReadSkipCount(Long readSkipCount) {
        this.readSkipCount = readSkipCount;
    }

    public Long getWriteSkipCount() {
        return writeSkipCount;
    }

    public void setWriteSkipCount(Long writeSkipCount) {
        this.writeSkipCount = writeSkipCount;
    }

    public Long getProcessSkipCount() {
        return processSkipCount;
    }

    public void setProcessSkipCount(Long processSkipCount) {
        this.processSkipCount = processSkipCount;
    }

    public Long getRollbackCount() {
        return rollbackCount;
    }

    public void setRollbackCount(Long rollbackCount) {
        this.rollbackCount = rollbackCount;
    }

    public String getExitCode() {
        return exitCode;
    }

    public void setExitCode(String exitCode) {
        this.exitCode = exitCode;
    }

    public String getExitMessage() {
        return exitMessage;
    }

    public void setExitMessage(String exitMessage) {
        this.exitMessage = exitMessage;
    }

    public Timestamp getLastUpdated() {
        return ObjectUtils.clone(lastUpdated);
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = ObjectUtils.clone(lastUpdated);
    }
}
