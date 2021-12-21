import { User } from '../user';
import { FuncaoDados } from './funcao-dados.model';

export class CommentFuncaoDados {
    constructor(
            public id?: number,
            public comment?: string,
            public user?: User,
            public funcaoDados?: FuncaoDados,
    ) {
    }
}