export class Rotina {

	constructor(public job_execution_id?: number,
				public version?: number,
				public job_instance_id?: number,
				public create_time?: Date,
				public start_time?: Date,
				public end_time?: Date,
				public status?: string,
				public exit_code?: string,
				public exit_message?: string,
				public last_updated?: Date,
				public job_configuration_location?: string) {
	}
}
