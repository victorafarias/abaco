import { FuncaoDados } from "../funcao-dados";
import { FuncaoTransacao } from "../funcao-transacao";
import { AbacoMensagens } from "../shared/mensagens.dto";

export class FuncaoImportarDTO{

	idDeflator: number;
	quantidadeINM: number;
	fundamentacao: string;
	idAnalise: number;
	funcoesParaImportar: any[];

}

export class ImportarFTDTO{
	abacoMensagens: AbacoMensagens;
	funcaoTransacao: FuncaoTransacao[] = [];
}

export class ImportarFDDTO{
	abacoMensagens: AbacoMensagens;
	funcaoDados: FuncaoDados[] = [];
}
