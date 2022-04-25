export class AbacoMensagens{
	public mensagens?: Mensagem[] = [];
}

export class Mensagem{
	public mensagem?: string;
	public tipo?: TipoMensagem;
}


export enum TipoMensagem{
	SUCESSO = "SUCESSO", AVISO = "AVISO", ERRO = "ERRO"
}
