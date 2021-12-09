export class NovidadeVersaoDTO{
    id?: number;
    versao?: string;
    novidades?: NovidadesDTO[] = [];
}


export class NovidadesDTO{
    id?: number;
    novidades?: string;
    ocorrencia?: string;
}