package br.com.basis.abaco.service.dto;

import br.com.basis.abaco.domain.BatchJobExecution;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;
import java.sql.Timestamp;

@NoArgsConstructor
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

    public Long getId() {
        return id;
    }

    public Timestamp getCreateTime() {
        return ObjectUtils.clone(createTime);
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = ObjectUtils.clone(createTime);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExitMessage() {
        return exitMessage;
    }

    public void setExitMessage(String exitMessage) {
        this.exitMessage = exitMessage;
    }

    public Long getVersion() {
        return version;
    }

    public Timestamp getEndTime() {
        return ObjectUtils.clone(endTime);
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = ObjectUtils.clone(endTime);
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(Long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJobConfigurationLocation() {
        return jobConfigurationLocation;
    }

    public void setJobConfigurationLocation(String jobConfigurationLocation) {
        this.jobConfigurationLocation = jobConfigurationLocation;
    }

    public Timestamp getStartTime() {
        return ObjectUtils.clone(startTime);
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = ObjectUtils.clone(startTime);
    }

    public Timestamp getLastUpdated() {
        return ObjectUtils.clone(lastUpdated);
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = ObjectUtils.clone(lastUpdated);
    }

    public String getExitCode() {
        return exitCode;
    }

    public void setExitCode(String exitCode) {
        this.exitCode = exitCode;
    }
}
