package br.com.basis.abaco.service.dto;

import br.com.basis.abaco.domain.BatchJobExecution;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@Setter
public class BatchJobExecutionDTO implements Serializable {

    private Long job_execution_id;
    private Long version;
    private Long job_instance_id;
    private Timestamp create_time;
    private Timestamp start_time;
    private Timestamp end_time;
    private String status;
    private String exit_code;
    private String exit_message;
    private Timestamp last_updated;
    private String job_configuration_location;

    public BatchJobExecutionDTO(BatchJobExecution batchJobExecution) {
        this.job_execution_id = batchJobExecution.getId();
        this.version = batchJobExecution.getVersion();
        this.job_instance_id = batchJobExecution.getJob_instance_id().getId();
        this.create_time = batchJobExecution.getCreate_time();
        this.start_time = batchJobExecution.getStart_time();
        this.end_time = batchJobExecution.getEnd_time();
        this.status = batchJobExecution.getStatus();
        this.exit_code = batchJobExecution.getExit_code();
        this.exit_message = batchJobExecution.getExit_message();
        this.last_updated = batchJobExecution.getLast_updated();
        this.job_configuration_location = batchJobExecution.getJob_configuration_location();
    }
}
