import { BaseEntity } from '../shared';


export class AnaliseShareEquipe implements BaseEntity {

  constructor(
    public id?: number,
    public equipeId?: number,
    public analisesId?: number[],
    public viewOnly?: boolean,
    public nomeEquipe?: string,
  ) {}
}
