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

    public String getStep_name() {
        return step_name;
    }

    public void setStep_name(String step_name) {
        this.step_name = step_name;
    }

    public BatchJobExecution getBatchJobExecution() {
        return batchJobExecution;
    }

    public void setBatchJobExecution(BatchJobExecution batchJobExecution) {
        this.batchJobExecution = batchJobExecution;
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

    public Long getCommit_count() {
        return commit_count;
    }

    public void setCommit_count(Long commit_count) {
        this.commit_count = commit_count;
    }

    public Long getRead_count() {
        return read_count;
    }

    public void setRead_count(Long read_count) {
        this.read_count = read_count;
    }

    public Long getFilter_count() {
        return filter_count;
    }

    public void setFilter_count(Long filter_count) {
        this.filter_count = filter_count;
    }

    public Long getWrite_count() {
        return write_count;
    }

    public void setWrite_count(Long write_count) {
        this.write_count = write_count;
    }

    public Long getRead_skip_count() {
        return read_skip_count;
    }

    public void setRead_skip_count(Long read_skip_count) {
        this.read_skip_count = read_skip_count;
    }

    public Long getWrite_skip_count() {
        return write_skip_count;
    }

    public void setWrite_skip_count(Long write_skip_count) {
        this.write_skip_count = write_skip_count;
    }

    public Long getProcess_skip_count() {
        return process_skip_count;
    }

    public void setProcess_skip_count(Long process_skip_count) {
        this.process_skip_count = process_skip_count;
    }

    public Long getRollback_count() {
        return rollback_count;
    }

    public void setRollback_count(Long rollback_count) {
        this.rollback_count = rollback_count;
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
}
