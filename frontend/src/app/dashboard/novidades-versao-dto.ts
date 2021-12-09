export class NovidadeVersaoDTO{
    id?: number;
    versao?: string;
    novidades?: NovidadesDTO[] = [];
}


export class NovidadesDTO{
    id?: number;
    novidade?: string;
    ocorrencia?: string;
}