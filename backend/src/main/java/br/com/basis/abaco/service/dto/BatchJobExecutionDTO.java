package br.com.basis.abaco.service.dto;

import br.com.basis.abaco.domain.BatchJobExecution;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@Setter
public class BatchJobExecutionDTO implements Serializable {

    private Long id;
    private Long version;
    private Long jobInstanceId;
    private Timestamp createTime;
    private Timestamp startTime;
    private Timestamp endTime;
    private String status;
    private String exitCode;
    private String exitMessage;
    private Timestamp lastUpdated;
    private String jobConfigurationLocation;

    public BatchJobExecutionDTO(BatchJobExecution batchJobExecution) {
        this.id = batchJobExecution.getId();
        this.version = batchJobExecution.getVersion();
        this.jobInstanceId = batchJobExecution.getJobInstance().getId();
        this.createTime = batchJobExecution.getCreateTime();
        this.startTime = batchJobExecution.getStartTime();
        this.endTime = batchJobExecution.getEndTime();
        this.status = batchJobExecution.getStatus();
        this.exitCode = batchJobExecution.getExitCode();
        this.exitMessage = batchJobExecution.getExitMessage();
        this.lastUpdated = batchJobExecution.getLastUpdated();
        this.jobConfigurationLocation = batchJobExecution.getJobConfigurationLocation();
    }

    public Timestamp getCreateTime() {
        return ObjectUtils.clone(createTime);
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = ObjectUtils.clone(createTime);
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

    public Timestamp getLastUpdated() {
        return ObjectUtils.clone(lastUpdated);
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = ObjectUtils.clone(lastUpdated);
    }
}
