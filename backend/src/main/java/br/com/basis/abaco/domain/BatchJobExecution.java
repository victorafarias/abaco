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
@Getter
@Setter
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
    private BatchJobInstance jobInstance;

    @NotNull
    @Column(name = "create_time")
    private Timestamp createTime;

    @Column(name = "start_time")
    private Timestamp startTime;

    @Column(name = "end_time")
    private Timestamp endTime;

    @Column(name = "status")
    private String status;

    @Column(name = "exit_code")
    private String exitCode;

    @Column(name = "exit_message")
    private String exitMessage;

    @Column(name = "last_updated")
    private Timestamp lastUpdated;

    @Column(name = "job_configuration_location")
    private String jobConfigurationLocation;

    @Override
    public String toString() {
        return "BatchJobExecution{" +
            "id=" + id +
            ", version=" + version +
            ", job_instance_id=" + jobInstance +
            ", create_time=" + createTime +
            ", start_time=" + startTime +
            ", end_time=" + endTime +
            ", status='" + status + '\'' +
            ", exit_code='" + exitCode + '\'' +
            ", exit_message='" + exitMessage + '\'' +
            ", last_updated=" + lastUpdated +
            ", job_configuration_location='" + jobConfigurationLocation + '\'' +
            '}';
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
