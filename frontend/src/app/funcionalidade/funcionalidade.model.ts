import { BaseEntity } from '../shared';

/**
 * Interface lightweight para quebrar ciclo de dependência runtime com Modulo.
 *
 * NOTA: Em runtime, esta propriedade pode conter uma instância completa de Modulo
 * (atribuída por modulo.model.ts), mas a tipagem como interface evita a importação
 * circular que causava warnings no Angular.
 */
export interface ModuloHandle extends BaseEntity {
  nome?: string;
}

export class Funcionalidade implements BaseEntity {

  constructor(
    public id?: number,
    public nome?: string,
    public modulo?: ModuloHandle,
    public funcaoDados?: BaseEntity,
    public funcaoTransacao?: BaseEntity,
    public artificialId?: number,
  ) { }

  static fromJSON(json: any) {
    return new Funcionalidade(json.id, json.nome, json.modulo,
      json.funcaoDados, json.FuncaoTransacao);
  }

  static toNonCircularJson(f: Funcionalidade): Funcionalidade {
    // Evita instanciar 'new Modulo()' para não depender da classe Modulo
    const modulo: ModuloHandle = f.modulo ? { id: f.modulo.id, nome: f.modulo.nome } : undefined;
    return new Funcionalidade(f.id, f.nome, modulo);
  }

  // XXX extrair interface?
  clone(): Funcionalidade {
    // shallow copy
    return new Funcionalidade(this.id, this.nome, this.modulo,
      this.funcaoDados, this.funcaoTransacao, this.artificialId);
  }
}
