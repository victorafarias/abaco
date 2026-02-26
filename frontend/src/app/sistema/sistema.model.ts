import { MappableEntities } from '../shared/mappable-entities';
import { BaseEntity } from '../shared';
import { Funcionalidade } from '../funcionalidade';
import { Modulo } from '../modulo';
import { Organizacao } from '../organizacao';

export enum TipoSistema {
  'NOVO' = 'NOVO',
  'LEGADO' = 'LEGADO'
}

export class Sistema implements BaseEntity {

  private mappableModulos: MappableEntities<Modulo>;

  constructor(
    public id?: number,
    public sigla?: string,
    public nome?: string,
    public tipoSistema?: TipoSistema,
    public numeroOcorrencia?: string,
    public organizacao?: Organizacao,
    public modulos?: Modulo[],
    public ehSustentacao?: boolean,
  ) {
    if (modulos) {
      this.mappableModulos = new MappableEntities<Modulo>(modulos);
      this.sortModulos();
    } else {
      this.mappableModulos = new MappableEntities<Modulo>();
    }
  }

  static fromJSON(json: any): Sistema {
    let modulos;
    if (json && json.modulos) {
      modulos = json.modulos.map(m => Modulo.fromJSON(m));
    }
    const newSistema = new Sistema(json.id, json.sigla,
      json.nome, json.tipoSistema, json.numeroOcorrencia, json.organizacao,
      modulos);
    return newSistema;
  }

  static toNonCircularJson(s: Sistema): Sistema {
    const nonCircularModulos = s.modulos ? s.modulos.map(m => Modulo.toNonCircularJson(m)) : [];
    return new Sistema(s.id, s.sigla, s.nome, s.tipoSistema, s.numeroOcorrencia,
      s.organizacao, nonCircularModulos);
  }

  private sortModulos() {
    if (this.modulos) {
      this.modulos.sort((a, b) => (a.nome || '').localeCompare(b.nome || ''));
    }
  }

  addModulo(modulo: Modulo) {
    this.mappableModulos.push(modulo);
    this.modulos = this.mappableModulos.values();
    this.sortModulos();
  }

  get funcionalidades(): Funcionalidade[] {
    if (!this.modulos) {
      return [];
    }
    const allFuncs = this.getAllFuncionalidadesAsArrayOfArrays();
    const result = allFuncs.reduce((a, b) => a.concat(b), []);
    return result.sort((a, b) => {
      const cmpModulo = (a.modulo?.nome || '').localeCompare(b.modulo?.nome || '');
      if (cmpModulo !== 0) return cmpModulo;
      return (a.nome || '').localeCompare(b.nome || '');
    });
  }

  private getAllFuncionalidadesAsArrayOfArrays(): Array<Array<Funcionalidade>> {
    const allFuncs = [];
    if (this.modulos) {
      this.modulos.forEach(m => allFuncs.push(this.retrieveFuncionalidadesFromModulo(m)));
    }
    return allFuncs;
  }

  private retrieveFuncionalidadesFromModulo(modulo: Modulo): Funcionalidade[] {
    if (modulo.funcionalidades) {
      modulo.funcionalidades.forEach(f => f.modulo = modulo);
      return modulo.funcionalidades;
    }
    return [];
  }

  addFuncionalidade(funcionalidade: Funcionalidade) {
    const modulo: Modulo = this.mappableModulos.get(funcionalidade.modulo as Modulo);
    modulo.addFuncionalidade(funcionalidade);
  }

  updateModulo(modulo: Modulo) {
    this.mappableModulos.update(modulo);
    this.modulos = this.mappableModulos.values();
    this.sortModulos();
  }

  deleteModulo(modulo: Modulo) {
    this.mappableModulos.delete(modulo);
    this.modulos = this.mappableModulos.values();
    this.sortModulos();
  }

  // XXX analisar pool único de funcionalidades vs estrutura OO
  // com um pool único de funcionalidades, o update funcionaria trivialmente
  // mas perderíamos a estrutura OO atual
  updateFuncionalidade(funcionalidade: Funcionalidade, oldFuncionalidade: Funcionalidade) {
    if (oldFuncionalidade.modulo !== funcionalidade.modulo) {
      this.updateFuncionalidadeWithDifferentModulo(funcionalidade, oldFuncionalidade);
    } else {
      this.doUpdateFuncionalidadeWithSameModule(funcionalidade);
    }
    this.modulos = this.mappableModulos.values();
    this.sortModulos();
  }

  private updateFuncionalidadeWithDifferentModulo(func: Funcionalidade, oldFunc: Funcionalidade) {
    this.doDeleteFuncionalidade(oldFunc);
    this.addFuncionalidadeAsNew(func);
  }

  private addFuncionalidadeAsNew(func: Funcionalidade) {
    func.id = undefined;
    func.artificialId = undefined;
    this.addFuncionalidade(func);
  }

  private doUpdateFuncionalidadeWithSameModule(func: Funcionalidade) {
    const modulo: Modulo = this.mappableModulos.get(func.modulo as Modulo);
    modulo.updateFuncionalidade(func);
  }

  deleteFuncionalidade(funcionalidade: Funcionalidade) {
    this.doDeleteFuncionalidade(funcionalidade);
    this.modulos = this.mappableModulos.values();
    this.sortModulos();
  }

  private doDeleteFuncionalidade(funcionalidade: Funcionalidade) {
    const modulo: Modulo = this.mappableModulos.get(funcionalidade.modulo as Modulo);
    modulo.deleteFuncionalidade(funcionalidade);
  }

}
