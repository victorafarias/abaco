import { FuncaoTransacao } from '.';
import { User } from '../user';

export class CommentFuncaoTransacao {
    constructor(
            public id?: number,
            public comment?: string,
            public user?: User,
            public funcaoDados?: FuncaoTransacao,
    ) {
    }
}