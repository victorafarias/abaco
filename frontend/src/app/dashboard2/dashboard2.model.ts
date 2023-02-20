import {MotivoAnalise} from "../divergencia";

export class DashBoard2{

	constructor(public motivoAnalise? : MotivoAnalise,
				public contaMotivo?: number,
				public cliente? : string,
				public contaCliente? : number,
				public pfDiferenca? : number,
				public dataDemanda? : string,
				public totalDemandas? : number,
				public pfDiferencaGlobal? : number) {
	}
}
