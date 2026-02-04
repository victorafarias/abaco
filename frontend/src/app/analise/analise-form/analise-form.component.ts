import { ActivatedRoute, Router } from '@angular/router';

import * as _ from 'lodash';
import { OnInit, Component } from '@angular/core';
import { AnaliseShareEquipe } from '../analise-share-equipe.model';
import { User } from '../../user/user.model';
import { UserService } from '../../user/user.service';
import { TipoEquipe } from 'src/app/tipo-equipe/tipo-equipe.model';
import { TipoEquipeService } from 'src/app/tipo-equipe/tipo-equipe.service';
import { Organizacao } from 'src/app/organizacao/organizacao.model';
import { OrganizacaoService } from 'src/app/organizacao/organizacao.service';
import { Contrato } from 'src/app/contrato/contrato.model';
import { ContratoService } from 'src/app/contrato/contrato.service';
import { Sistema } from 'src/app/sistema/sistema.model';
import { SistemaService } from 'src/app/sistema/sistema.service';
import { EsforcoFase } from 'src/app/esforco-fase/esforco-fase.model';
import { SelectItem, ConfirmationService } from 'primeng';
import { Manual } from 'src/app/manual/manual.model';
import { ManualService } from 'src/app/manual/manual.service';
import { MessageUtil } from 'src/app/util/message.util';
import { Subscription, Observable } from 'rxjs';
import { AnaliseSharedDataService } from 'src/app/shared/analise-shared-data.service';
import { PageNotificationService } from '@nuvem/primeng-components';
import { Analise } from '../analise.model';
import { MetodoContagem } from '../analise.model';
import { ManualContrato } from 'src/app/organizacao/ManualContrato.model';
import { FatorAjuste } from 'src/app/fator-ajuste/fator-ajuste.model';
import { FatorAjusteLabelGenerator } from 'src/app/shared/fator-ajuste-label-generator';
import { FuncaoDados } from '../../funcao-dados/funcao-dados.model';
import { AnaliseService } from '../analise.service';
import { FuncaoDadosService } from '../../funcao-dados/funcao-dados.service';
import { FuncaoTransacaoService } from '../../funcao-transacao/funcao-transacao.service';
import { element } from 'protractor';
import { StatusService } from 'src/app/status/status.service';
import { Status } from 'src/app/status/status.model';


@Component({
    selector: 'jhi-analise-form',
    templateUrl: './analise-form.component.html',
    providers: [ConfirmationService, AnaliseSharedDataService]
})
export class AnaliseFormComponent implements OnInit {

    // Alterado: Lista de status que impedem a edição do método
    private readonly STATUS_BLOQUEANTES = [
        'Aprovado', 'Aprovada', 'Concluído', 'Concluída',
        'Fechada', 'Fechado', 'Finalizado', 'Finalizada'
    ];

    isEdicao: boolean;
    isClone: boolean = false;
    canEditMetodo: boolean = false;
    disableFuncaoTrasacao = true;
    disableAba: boolean;
    equipeShare = [];
    analiseShared: Array<AnaliseShareEquipe> = [];
    selectedEquipes: Array<AnaliseShareEquipe>;
    selectedToDelete: AnaliseShareEquipe;
    mostrarDialog = false;
    isEdit: boolean;
    isSaving: boolean;
    dataAnalise: any;
    dataEncerramento: any;
    dataHomologacao: any;
    dataCriacao: any;
    loggedUser: User;
    tipoEquipesLoggedUser: TipoEquipe[] = [];
    diasGarantia: number;
    public validacaoCampos: boolean;
    aguardarGarantia: boolean;
    enviarParaBaseLine: boolean;
    organizacoes: Organizacao[];
    contratos: Contrato[];
    sistemas: Sistema[];
    esforcoFases: EsforcoFase[] = [];
    metodosContagem: SelectItem[] = [];
    fatoresAjuste: SelectItem[] = [];
    equipeResponsavel: TipoEquipe[] = [];
    lstfuncaoDadosDto: FuncaoDados[];
    nomeManual = this.getLabel('Selecione um contrato');
    manual: Manual;
    manuais: Manual[] = [];
    users: User[] = [];
    usersDropDown: User[] = [];
    manuaisCombo: SelectItem[] = [];
    statusCombo: Status[] = [];
    showFuncaoDados: Boolean = false;


    tiposAnalise: SelectItem[] = [
        { label: MessageUtil.CONTAGEM_APLICACAO, value: MessageUtil.APLICACAO },
        { label: MessageUtil.PROJETO_DESENVOLVIMENTO, value: MessageUtil.DESENVOLVIMENTO },
        { label: MessageUtil.PROJETO_MELHORIA, value: MessageUtil.MELHORIA }

    ];


    metodoContagem: SelectItem[] = [
        { label: MessageUtil.DETALHADA_IFPUG, value: MessageUtil.DETALHADA_IFPUG },
        { label: MessageUtil.INDICATIVA_NESMA, value: MessageUtil.INDICATIVA_NESMA },
        { label: MessageUtil.ESTIMADA_NESMA, value: MessageUtil.ESTIMADA_NESMA }
    ];

    private routeSub: Subscription;
    private subscription: Subscription;
    private saveSubscription: Subscription;
    public hideShowSelectEquipe: boolean;

    constructor(
        private router: Router,
        private route: ActivatedRoute,
        private analiseService: AnaliseService,
        private sistemaService: SistemaService,
        private analiseSharedDataService: AnaliseSharedDataService,
        private equipeService: TipoEquipeService,
        private organizacaoService: OrganizacaoService,
        private pageNotificationService: PageNotificationService,
        private userService: UserService,
        private contratoService: ContratoService,
        private manualService: ManualService,
        private statusService: StatusService,
    ) {
    }

    ngOnInit() {
        this.disableAba = true;
        this.validacaoCampos = true;
        this.hideShowSelectEquipe = true;
        this.analiseSharedDataService.init();
        this.isEdicao = false;
        this.isSaving = false;
        this.dataHomologacao = new Date();
        this.dataCriacao = new Date();
        this.getOrganizationsFromActiveLoggedUser();
        this.getLoggedUserId();
        this.getEquipesFromActiveLoggedUser();
        this.habilitarCamposIniciais();
        this.getAnalise();
        this.traduzirtiposAnalise();
        this.traduzirMetodoContagem();
        this.getLstStatus();
    }

    getLabel(label) {
        return label;
    }

    traduzirtiposAnalise() {
        // this.translate.stream(['Analise.Analise.TiposAnalise.ProjetoDesenvolvimento', 'Analise.Analise.TiposAnalise.ProjetoMelhoria',
        //     'Analise.Analise.TiposAnalise.ContagemAplicacao']).subscribe((traducao) => {
        //     this.tiposAnalise = [
        //         {label: traducao['Analise.Analise.TiposAnalise.ProjetoDesenvolvimento'], value: 'DESENVOLVIMENTO'},
        //         {label: traducao['Analise.Analise.TiposAnalise.ProjetoMelhoria'], value: 'MELHORIA'},
        //         {label: traducao['Analise.Analise.TiposAnalise.ContagemAplicacao'], value: 'APLICACAO'}
        //     ];

        // });
    }

    traduzirMetodoContagem() {
        // this.translate.stream(['Analise.Analise.metsContagens.DETALHADA_IFPUG', 'Analise.Analise.metsContagens.INDICATIVA_NESMA',
        //     'Analise.Analise.metsContagens.ESTIMADA_NESMA']).subscribe((traducao) => {
        //     this.metodoContagem = [
        //         {label: traducao['Analise.Analise.metsContagens.DETALHADA_IFPUG'], value: 'DETALHADA'},
        //         {label: traducao['Analise.Analise.metsContagens.INDICATIVA_NESMA'], value: 'INDICATIVA'},
        //         {label: traducao['Analise.Analise.metsContagens.ESTIMADA_NESMA'], value: 'ESTIMADA'}
        //     ];

        // });
    }

    getOrganizationsFromActiveLoggedUser() {
        this.organizacaoService.dropDownActiveLoggedUser().subscribe(res => {
            this.organizacoes = res;

            // Auto-seleciona se houver apenas uma organização disponível
            if (!this.isEdicao && this.organizacoes && this.organizacoes.length === 1) {
                this.analise.organizacao = this.organizacoes[0];
                this.setSistemaOrganizacao(this.organizacoes[0]);
            }
        });
    }

    getLoggedUserId() {
        this.userService.getLoggedUserWithId().subscribe(res => {
            this.loggedUser = res;
        });
    }

    getEquipesFromActiveLoggedUser() {
        this.equipeService.getEquipesActiveLoggedUser().subscribe(res => {
            this.tipoEquipesLoggedUser = res;
        });
    }
    getLstStatus() {
        this.statusService.listActive().subscribe(
            lstStatus => {
                this.statusCombo = lstStatus;
                this.setDefaultStatus();
            });
    }

    private setDefaultStatus() {
        if ((!this.analise.id || this.isClone) && this.statusCombo && this.statusCombo.length > 0) {
            const statusEmAnalise = this.statusCombo.find(s => s.nome === 'Em Análise');
            if (statusEmAnalise) {
                this.analise.status = statusEmAnalise;
            }
        }
    }

    checkUserAnaliseEquipes() {
        let retorno = false;
        if (this.tipoEquipesLoggedUser) {
            this.tipoEquipesLoggedUser.forEach(equipe => {
                if (equipe.id === this.analise.equipeResponsavel.id) {
                    retorno = true;
                }
            });
        }
        return retorno;
    }

    checkIfUserCanEdit() {
        let retorno = false;
        if (this.tipoEquipesLoggedUser && this.analise.compartilhadas) {
            this.tipoEquipesLoggedUser.forEach(equipe => {
                this.analise.compartilhadas.forEach(compartilhada => {
                    if (equipe.id === compartilhada.equipeId) {
                        if (!compartilhada.viewOnly) {
                            retorno = true;
                        }
                    }
                });
            });
        }
        return retorno;
    }

    getAnalise() {
        this.routeSub = this.route.params.subscribe(params => {
            if (params['id']) {
                this.isEdicao = true;
                // Alterado: Verifica se é uma clonagem
                if (params['clone']) {
                    this.isClone = true;
                }
                this.analiseService.find(params['id']).subscribe(analise => {
                    // Alterado: Define se pode editar o método baseado no status
                    this.checkIfCanEditMetodo(analise);

                    if (analise.pfTotal <= 0) {
                        this.canEditMetodo = true;
                    }
                    analise = new Analise().copyFromJSON(analise);
                    this.loadDataAnalise(analise);
                    if (!(this.verifyCanEditAnalise(analise))) {
                        this.pageNotificationService.addErrorMessage('Você não tem permissão para editar esta análise, redirecionando para a tela de visualização...');
                        this.router.navigate(['/analise', analise.id, 'view']);
                    }
                    this.disableFuncaoTrasacao = analise.metodoContagem === MessageUtil.INDICATIVA;
                    // Alterado: Define status padrão se for clonagem
                    if (this.isClone) {
                        this.setDefaultStatus();
                    }
                },
                    err => {
                        this.pageNotificationService.addErrorMessage(
                            this.getLabel('Você não tem permissão para editar esta análise, redirecionando para a tela de visualização...')
                        );
                        this.router.navigate(['/analise']);
                    });

            } else {
                // Cria uma nova análise com valores padrão
                this.analise = new Analise();
                this.analise.status = new Status();
                this.analise.esforcoFases = [];
                this.analise.enviarBaseline = true;
                this.analise.fatorCriticidade = false;
                this.canEditMetodo = true;

                // Define valores padrão para Propósito e Escopo da Contagem (editáveis pelo usuário)
                this.analise.propositoContagem = 'Medir o tamanho do projeto de melhoria da OS/Sprint.';
                this.analise.escopo = 'Funções impactadas conforme os requisitos identificados nos insumos apresentados.';

                this.setDefaultStatus();
            }
        });
    }

    // Alterado: Novo método auxiliar para verificar status
    private checkIfCanEditMetodo(analise: Analise) {
        if (analise.status && this.STATUS_BLOQUEANTES.includes(analise.status.nome)) {
            this.canEditMetodo = false;
        } else {
            this.canEditMetodo = true;
        }
    }

    private verifyCanEditAnalise(analise: Analise): Boolean {
        let canEditAnalise: Boolean = false;
        this.tipoEquipesLoggedUser.forEach(equip => {
            if (equip.id === analise.equipeResponsavel.id) {
                canEditAnalise = true;
            }
        });
        analise.compartilhadas.forEach(comp => {
            this.tipoEquipesLoggedUser.forEach(equip => {
                if (equip.id === comp.id && !(comp.viewOnly)) {
                    canEditAnalise = true;
                }
            });
        });
        return canEditAnalise;
    }

    getGarantia(): any {
        this.diasGarantia = this.diasGarantia !== undefined ? this.analise.contrato.diasDeGarantia : undefined;
    }

    private inicializaValoresAposCarregamento(analiseCarregada: Analise) {
        if (analiseCarregada.bloqueiaAnalise) {
            this.pageNotificationService.addErrorMessage(this.getLabel('Você não pode editar uma análise bloqueada!'));
            this.router.navigate(['/analise']);
        }
        this.analise = analiseCarregada;
        if (!this.checkIfUserCanEdit() && !this.checkUserAnaliseEquipes()) {
            this.pageNotificationService
                .addErrorMessage(this.getLabel('msgSemVocê não tem permissão para editar esta análise, redirecionando para a tela de visualização...PermissaoParaEditarAnalise'));
            this.router.navigate([`/analise/${analiseCarregada.id}/view`]);
        }
        this.setSistemaOrganizacao(analiseCarregada.organizacao);
        if (analiseCarregada.contrato !== undefined && analiseCarregada.contrato.manualContrato) {
            this.setManual(analiseCarregada.manual ? analiseCarregada.manual : new Manual());
        }
    }

    getSistemaSelecionado() {
        this.analiseSharedDataService.sistemaSelecionado();
    }

    setSistemaOrganizacao(org: Organizacao) {
        if (!this.isEdicao) {
            this.analise = new Analise();
            this.analise.manual = new Manual();
            this.analise.organizacao = org;
            this.analise.dataCriacaoOrdemServico = new Date();

            // Define valores padrão para Propósito e Escopo da Contagem (editáveis pelo usuário)
            this.analise.propositoContagem = 'Medir o tamanho do projeto de melhoria da OS/Sprint.';
            this.analise.escopo = 'Funções impactadas conforme os requisitos identificados nos insumos apresentados.';

            this.setDefaultStatus();
        }
        this.contratoService.findAllContratoesByOrganization(org).subscribe((contracts) => {
            this.contratos = contracts;

            // Auto-seleciona contrato se houver apenas um disponível
            if (!this.isEdicao && this.contratos && this.contratos.length === 1) {
                this.analise.contrato = this.contratos[0];
                this.contratoSelected(this.contratos[0]);
            }
        });
        this.sistemaService.findAllSystemOrg(org.id).subscribe({
            next: (res: Sistema[]) => {
                // Ordenar alfabeticamente por nome
                this.sistemas = res.sort((a, b) => {
                    const nomeA = (a.nome || '').toLowerCase();
                    const nomeB = (b.nome || '').toLowerCase();
                    return nomeA.localeCompare(nomeB, 'pt-BR');
                });

                // Auto-seleciona sistema se houver apenas um disponível
                if (!this.isEdicao && this.sistemas && this.sistemas.length === 1) {
                    this.analise.sistema = this.sistemas[0];
                }
            },
            error: (err) => {
                console.error('Erro ao carregar sistemas:', err);
            }
        });
        this.setEquipeOrganizacao(org);
    }

    setEquipeOrganizacao(org: Organizacao) {
        this.equipeService.findAllEquipesByOrganizacaoIdAndLoggedUser(org.id).subscribe((res: TipoEquipe[]) => {
            this.equipeResponsavel = res;
            if (this.equipeResponsavel !== null) {
                this.hideShowSelectEquipe = false;

                // Auto-seleciona equipe baseado nas equipes vinculadas ao usuário logado
                // Se o usuário está vinculado a exatamente uma equipe, seleciona-a como padrão
                if (!this.isEdicao && this.tipoEquipesLoggedUser && this.tipoEquipesLoggedUser.length === 1) {
                    // Verifica se a equipe do usuário está disponível para a organização selecionada
                    const equipeUsuario = this.equipeResponsavel.find(
                        eq => eq.id === this.tipoEquipesLoggedUser[0].id
                    );
                    if (equipeUsuario) {
                        this.analise.equipeResponsavel = equipeUsuario;
                    }
                }
                // Se o usuário está vinculado a mais de uma equipe ou nenhuma, não seleciona nenhum valor default
            }
        });
    }

    setManual(manualSelected: Manual) {
        if (manualSelected) {
            this.manualService.find(manualSelected.id).subscribe((manual) => {
                this.nomeManual = manual.nome;
                this.carregarEsforcoFases(manual);
                this.carregarMetodosContagem(manual);
                this.inicializaFatoresAjuste(manual);
                this.manualSelecionado(manual);
                this.setManuais(this.analise.contrato);
                this.carregaFatorAjusteNaEdicao();
            });
        }
    }

    private inicializaFatoresAjuste(manual: Manual) {
        this.fatoresAjuste = [];
        if (manual.fatoresAjuste) {
            const faS: FatorAjuste[] = _.cloneDeep(manual.fatoresAjuste);
            faS.sort((n1, n2) => (n1.ordem || 0) - (n2.ordem || 0));
            faS.forEach(fa => {
                const label = FatorAjusteLabelGenerator.generate(fa);
                this.fatoresAjuste.push({ label, value: fa });
            });
            this.fatoresAjuste.unshift({ label: this.getLabel('Nenhum'), value: null });
        }
    }

    private carregaFatorAjusteNaEdicao() {
        if (this.analise.fatorAjuste) {
            this.analise.fatorAjuste = this.fatoresAjuste.find(
                (f) => f !== null && f.value !== null && f.value.id === this.analise.fatorAjuste.id
            ).value;
        }
    }

    private carregarEsforcoFases(manual: Manual) {
        manual.esforcoFases.forEach(element => {
            this.esforcoFases.push(new EsforcoFase().copyFromJSON(element));
        });;

        if (this.isEdicao && !(this.analise.esforcoFases)) {
            // Traz todos esforcos de fases selecionados
            this.analise.esforcoFases = _.cloneDeep(manual.esforcoFases);
        }
    }

    private carregarMetodosContagem(manual: Manual) {
        this.metodosContagem = [
            {
                value: MessageUtil.DETALHADA,
                label: this.getLabel('Detalhada (IFPUG)')
            },
            {
                value: MessageUtil.ESTIMADA,
                label: this.getLabelValorVariacao(
                    this.getLabel('Estimada (NESMA)'),
                    manual.valorVariacaoEstimada)
            },
            {
                value: MessageUtil.INDICATIVA,
                label: this.getLabelValorVariacao(
                    this.getLabel('Indicativa (NESMA)'),
                    manual.valorVariacaoIndicativa)
            }
        ];
    }

    private getLabelValorVariacao(label: string, valorVariacao: number): string {
        return label + ' - ' + valorVariacao.toLocaleString() + '%';
    }

    totalEsforcoFases() {
        const initialValue = 0;
        if (this.analise.esforcoFases) {
            return this.analise.esforcoFases
                .reduce((val, ef) => val + ef.esforco, initialValue);
        }
        return initialValue;
    }

    sistemaDropdownPlaceholder() {
        if (this.sistemas) {
            if (this.sistemas.length > 0) {
                return this.getLabel('Selecione');
            } else {
                return this.getLabel('Organização não possui nenhum Sistema cadastrado');
            }
        } else {
            return this.getLabel('Selecione uma Organização');
        }
    }

    disabledOptionsforEdit() {
        if (this.analise.id && this.analise.id > 0) {
            return true;
        } else {
            return false;
        }
    }
    disabledTipoContagemEdit() {
        if (this.canEditMetodo) {
            return false;
        } else {
            return true;
        }
    }

    isContratoSelected(): boolean {
        return this.analiseSharedDataService.isContratoSelected();
    }

    contratoSelected(contrato: Contrato) {
        if (contrato && contrato.manualContrato) {
            this.setManuais(contrato);
            let manualSelected;
            if ((this.analise.manual) && (typeof this.analise.manual.id !== 'undefined')) {
                manualSelected = this.analise.manual;
            } else {
                manualSelected = contrato.manualContrato[0].manual;
            }
            this.setManual(manualSelected);
            this.analise.manual = manualSelected;
            this.diasGarantia = this.analise.contrato.diasDeGarantia;
            this.carregarMetodosContagem(manualSelected);
        }
    }

    setManuais(contrato: Contrato) {
        contrato.manualContrato.forEach(item => {
            item.dataInicioVigencia = new Date(item.dataInicioVigencia);
            item.dataFimVigencia = new Date(item.dataFimVigencia);
        });

        this.ordenarManuais(contrato);
        this.resetManuais();
        this.populaComboManual(contrato);
    }

    private populaComboManual(contrato: Contrato) {
        contrato.manualContrato.forEach((item: ManualContrato) => {
            const entity: Manual = new Manual();
            const m: Manual = entity.copyFromJSON(item.manual);
            this.manuais.push(item.manual);
            this.manuaisCombo.push({
                label: `${m.nome} ${this.formataData(item.dataInicioVigencia)} - ` +
                    `${this.formataData(item.dataFimVigencia)}`
                    + this.formataBoleano(item.ativo),
                value: this.analise.manual && m.id === this.analise.manual.id ? this.analise.manual : m
            });
        });
    }

    private ordenarManuais(contrato: Contrato) {
        contrato.manualContrato = contrato.manualContrato.sort((a, b): number => {
            if (b.ativo && a.ativo) {

                if ((a.dataInicioVigencia.getTime() === b.dataInicioVigencia.getTime())) {
                    if (a.dataFimVigencia.getTime() < b.dataFimVigencia.getTime()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
                if (a.dataInicioVigencia.getTime() < b.dataInicioVigencia.getTime()) {
                    return -1;
                }
                return 1;
            } else if (!(a.ativo) && b.ativo) {
                return 1;
            } else {
                return -1;
            }
        });
    }

    resetManuais() {
        this.manuais = [];
        this.manuaisCombo = [];
    }

    manualSelecionado(manual: Manual) {
        this.esforcoFases = _.cloneDeep(manual.esforcoFases);
        if (!this.isEdicao && !(this.analise.esforcoFases)) {
            // Traz todos esforcos de fases selecionados
            this.analise.esforcoFases = _.cloneDeep(manual.esforcoFases);
        }
    }

    private formataData(data: Date): String {
        const dt = `${data.getDay()}/${(data.getMonth() + 1)}/${data.getFullYear()}`;
        return dt;
    }

    private formataBoleano(bool: Boolean): String {
        return bool ? this.getLabel(' Ativo') : this.getLabel(' Inativo');
    }

    save() {
        this.validaCamposObrigatorios();
        if (this.verificarCamposObrigatorios()) {
            if (this.analise.id && this.analise.id > 0) {
                this.analiseService.update(this.analise).subscribe(() => {
                    this.pageNotificationService.addSuccessMessage(
                        this.isEdit ? this.getLabel('Registro salvo com sucesso!') :
                            this.getLabel('Dados alterados com sucesso!'));
                    this.diasGarantia = this.analise.contrato.diasDeGarantia;
                });
            } else {
                this.analiseService.create(this.analise).subscribe(res => {
                    this.analise = res;
                    this.pageNotificationService.addSuccessMessage(this.getLabel('Análise salva com sucesso'));
                    this.router.navigate(['/analise', this.analise.id, 'edit']);
                });
            }
        }
    }

    private validaCamposObrigatorios() {
        const validacaoIdentificadorAnalise = this.analise.identificadorAnalise ? true : false;
        const validacaoContrato = this.analise.contrato ? true : false;
        const validacaoMetodoContagem = this.analise.metodoContagem ? true : false;
        const validacaoTipoAnallise = this.analise.tipoAnalise ? true : false;
        this.validacaoCampos = !(validacaoIdentificadorAnalise === true
            && validacaoContrato === true
            && validacaoMetodoContagem === true
            && validacaoTipoAnallise === true);
        this.enableDisableAba();
    }

    enableDisableAba() {
        if (this.validacaoCampos === false) {
            this.disableAba = false;
            this.disableFuncaoTrasacao = this.analise.metodoContagem === MessageUtil.INDICATIVA;
        }
    }

    private verificarCamposObrigatorios(): boolean {
        let isValid = true;
        if (!this.analise.identificadorAnalise) {
            this.pageNotificationService.addErrorMessage(this.getLabel('Informe o campo Identificador da Analise para continuar.'));
            isValid = false;
        }
        if (!this.analise.contrato) {
            this.pageNotificationService.addErrorMessage(this.getLabel('Informe o Contrato para continuar'));
            isValid = false;
        }
        if (!this.analise.numeroOs) {
            this.pageNotificationService.addErrorMessage(this.getLabel('Informe o número da OS para continuar'));
            isValid = false;
        }
        if (!this.analise.metodoContagem) {
            this.pageNotificationService.addErrorMessage(this.getLabel('Informe o Método de Contagem para continuar'));
            isValid = false;
        }
        if (!this.analise.manual) {
            this.pageNotificationService.addErrorMessage(this.getLabel('Informe o Tipo de Contagem para continuar'));
            isValid = false;
        }
        if (!this.analise.tipoAnalise) {
            this.pageNotificationService.addErrorMessage(this.getLabel('Informe o Tipo de Contagem para continuar'));
            isValid = false;
        }
        if ((!this.analise.users || this.analise.users.length <= 0) && this.analise.id && this.analise.id > 0) {
            this.pageNotificationService.addErrorMessage(this.getLabel('Deve haver ao menos um usuário para continuar'));
            isValid = false;
        }
        if ((!this.analise.esforcoFases || this.analise.esforcoFases.length <= 0) && this.analise.id && this.analise.id > 0) {
            this.pageNotificationService.addErrorMessage(this.getLabel('Deve haver ao menos uma Fase para continuar'));
            isValid = false;
        }
        if (!(this.analise.status && this.analise.status.id)) {
            this.pageNotificationService.addErrorMessage(this.getLabel('Informe o Status da Analise para continuar'));
            isValid = false;
        }
        if (this.analise.fatorCriticidade === true && !this.analise.valorCriticidade) {
            this.pageNotificationService.addErrorMessage(this.getLabel('Informe o valor da criticidade para continuar'));
            isValid = false;
        }
        return isValid;
    }

    public habilitarCamposIniciais() {
        return this.isEdicao;
    }

    get analise(): Analise {
        return this.analiseSharedDataService.analise;
    }


    set analise(analise: Analise) {
        this.analiseSharedDataService.analise = analise;
    }

    public openCompartilharDialog() {
        if (this.checkUserAnaliseEquipes()) {
            this.equipeService.findAllCompartilhaveis(this.analise.organizacao.id,
                this.analise.id,
                this.analise.equipeResponsavel.id).subscribe((equipes) => {
                    if (equipes) {
                        equipes.forEach((equipe) => {
                            const entity: AnaliseShareEquipe = Object.assign(new AnaliseShareEquipe(),
                                {
                                    id: undefined,
                                    equipeId: equipe.id,
                                    analisesId: [this.analise.id],
                                    viewOnly: true,
                                    nomeEquipe: equipe.nome
                                });
                            this.equipeShare.push(entity);
                        });
                    }
                });
            this.analiseService.findAllCompartilhadaByAnalise(this.analise.id).subscribe((shared) => {
                this.analiseShared = shared;
            });
            this.mostrarDialog = true;
        } else {
            this.pageNotificationService.addErrorMessage(this.getLabel('Somente membros da equipe responsável podem compartilhar esta análise!'));
        }
    }

    public salvarCompartilhar() {
        if (this.selectedEquipes && this.selectedEquipes.length !== 0) {
            this.analiseService.salvarCompartilhar(this.selectedEquipes, false).subscribe((res) => {
                this.mostrarDialog = false;
                this.limparSelecaoCompartilhar();
            });
        } else {
            this.pageNotificationService.addInfoMessage(this.getLabel('Selecione pelo menos um registro para poder adicionar ou clique no X para sair!'));
        }

    }

    public deletarCompartilhar() {
        if (this.selectedToDelete && this.selectedToDelete !== null) {
            this.analiseService.deletarCompartilhar(this.selectedToDelete.id).subscribe((res) => {
                this.mostrarDialog = false;
                this.pageNotificationService.addSuccessMessage(this.getLabel('Compartilhamento removido com sucesso!'));
                this.limparSelecaoCompartilhar();
            });
        } else {
            this.pageNotificationService.addInfoMessage(this.getLabel('Selecione pelo menos um registro para poder remover ou clique no X para sair!'));
        }
    }

    public limparSelecaoCompartilhar() {
        this.getAnalise();
        this.selectedEquipes = undefined;
        this.selectedToDelete = undefined;
    }

    public updateViewOnly() {
        setTimeout(() => {
            this.analiseService.atualizarCompartilhar(this.selectedToDelete).subscribe((res) => {
                this.pageNotificationService.addSuccessMessage(this.getLabel('Registro atualizado com sucesso!'));
            });
        }, 250);
    }

    private populaComboUsers() {
        if (this.analise.id) {
            this.userService.getAllUsers(this.analise.organizacao, this.analise.equipeResponsavel).subscribe(usuarios => {
                usuarios.forEach(usuario => {
                    this.usersDropDown.push(new User().copyFromJSON(usuario));
                });
                if (this.users && this.users.length > 0) {
                    this.users = _.clone(this.analise.users);
                    this.users = this.users.concat(this.usersDropDown.filter(user => {
                        return !this.analise.users.some(usuario => user.id === usuario.id);
                    }));
                } else {
                    this.users = this.usersDropDown;
                }
                if (this.analise.users && this.analise.users.length === 0) {
                    const user = _.find(this.users, { id: this.loggedUser });
                    this.analise.users.push(user);
                }
            });
        }
    }

    private subscribeToSaveResponse(result: Observable<any>) {
        this.saveSubscription = result.subscribe((res) => {
            this.analise = res;
            this.pageNotificationService.addSuccessMessage(this.getLabel('Análise salva com sucesso'));
            this.router.navigate(['/analise', this.analise.id, 'edit']);
        });
    }

    alterarMetodoContagem() {
        if (this.isEdicao) {
            // Verifica status antes de prosseguir
            if (this.analise.status && this.STATUS_BLOQUEANTES.includes(this.analise.status.nome)) {
                this.pageNotificationService.addErrorMessage(
                    `Não é possível alterar o Método da Contagem em Análises que estão com status ${this.analise.status.nome}`
                );
                // Reverter para o valor anterior seria ideal aqui, recarregando a análise
                this.getAnalise();
                return;
            }

            // Lógica visual para abas
            if (this.analise.metodoContagem === MessageUtil.INDICATIVA) {
                this.disableFuncaoTrasacao = true;
            } else {
                this.disableFuncaoTrasacao = false;
            }

            // Feedback visual de que os valores serão recalculados ao salvar
            this.pageNotificationService.addInfoMessage(
                'Os valores de complexidade e PF serão recalculados conforme o novo método ao salvar.'
            );

        } else {
            // Comportamento para nova análise (sem id)
            if (this.analise.metodoContagem === MetodoContagem.DETALHADA) {
                this.analise.enviarBaseline = true;
            } else {
                this.analise.enviarBaseline = false;
            }
        }
    }

    private loadDataAnalise(analise: Analise) {
        this.inicializaValoresAposCarregamento(analise);
        this.dataAnalise = this.analise;

        this.aguardarGarantia = this.analise.baselineImediatamente;
        this.enviarParaBaseLine = this.analise.enviarBaseline;
        this.analise.fatorCriticidade = analise.fatorCriticidade;
        this.diasGarantia = this.getGarantia();
        this.contratoSelected(this.analise.contrato);
        this.populaComboUsers();
        this.validaCamposObrigatorios();
        this.analiseSharedDataService.analiseCarregada();
    }

    handleChange(e) {
        const index = e.index;
        let link;
        switch (index) {
            case 0:
                return;
            case 1:
                link = ['/analise/' + this.analise.id + '/funcao-dados'];
                break;
            case 2:
                link = ['/analise/' + this.analise.id + '/funcao-transacao'];
                break;
            case 3:
                link = ['/analise/' + this.analise.id + '/resumo'];
                break;
        }
        this.router.navigate(link);
    }

    getIdentificadorAnalise() {
        return this.analise.analiseDivergence.identificadorAnalise;
    }

    goToDivergencia(divergencia) {
        if (divergencia.bloqueiaAnalise === true) {
            window.open("#/divergencia/" + divergencia.id + "/view", "_blank");
        } else {
            window.open("#/divergencia/" + divergencia.id + "/edit", "_blank");
        }
    }
}

