export class Visaopf{
    public showProcess:boolean = true
    public atualizar:boolean
    public processosContagem:any
    public qtdFinalizados:number = 0
    public tela: Tela = new Tela()
    public telaResult:any
    public uuidProcesso:any
    public tiposComponents:Array<any>
    public canvasWidth=1300
    public canvasHeight=550
    public proporcaoW: any
    public proporcaoH: any
    public componentTooltip:any
    public tooltip: HTMLElement
    public cenario: Cenario = new Cenario()
}

export class Cenario {
    public id: string
    public analise: any
    public nome: string
    public telas: Array<Tela> = []
    public telasResult: Array<any> = []
    public itenContagem: Array<any>
}

export class Tela {
    public id: string
    public originalImageName: string
    public tipo: string
    public imagem: File
    public size: any
    public bucketName: any
    public dataUrl: any
    public componentes: Array<Componente> = []
    public tipos: string
    public analise_id: number
}

export class Componente {
    public id: string
    public nome: string
    public descricao: string
    public tipo: string
    public score: number
    public color: string
    public coordenada = new Coordenada()
}

export class Coordenada {
    public id: string
    public xmin: number
    public xmax: number
    public ymin: number
    public ymax: number

    setCoordenadas(xmin, ymin, xmax, ymax) {
        this.xmin = parseInt(xmin)
        this.ymin = parseInt(ymin)
        this.xmax = parseInt(xmax)
        this.ymax = parseInt(ymax)
    }
}

