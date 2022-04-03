import { Analise } from "../analise";
import { User } from "../user";

export class HistoricoDTO{
	id?: number;
	analise?: Analise;
	dtAcao?: any;
	usuario?: User;
	acao?: string;
}
