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

    public BatchJobInstance getJob_instance_id() {
        return job_instance_id;
    }

    public void setJob_instance_id(BatchJobInstance job_instance_id) {
        this.job_instance_id = job_instance_id;
    }

    public Timestamp getCreate_time() {
        return ObjectUtils.clone(create_time);
    }

    public void setCreate_time(Timestamp create_time) {
        this.create_time = ObjectUtils.clone(create_time);
    }

    public Timestamp getStart_time() {
        return ObjectUtils.clone(start_time);
    }

    public void setStart_time(Timestamp start_time) {
        this.start_time = ObjectUtils.clone(start_time);
    }

    public Timestamp getEnd_time() {
        return ObjectUtils.clone(end_time);
    }

    public void setEnd_time(Timestamp end_time) {
        this.end_time = ObjectUtils.clone(end_time);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExit_code() {
        return exit_code;
    }

    public void setExit_code(String exit_code) {
        this.exit_code = exit_code;
    }

    public String getExit_message() {
        return exit_message;
    }

    public void setExit_message(String exit_message) {
        this.exit_message = exit_message;
    }

    public Timestamp getLast_updated() {
        return ObjectUtils.clone(last_updated);
    }

    public void setLast_updated(Timestamp last_updated) {
        this.last_updated = ObjectUtils.clone(last_updated);
    }

    public String getJob_configuration_location() {
        return job_configuration_location;
    }

    public void setJob_configuration_location(String job_configuration_location) {
        this.job_configuration_location = job_configuration_location;
    }
}
