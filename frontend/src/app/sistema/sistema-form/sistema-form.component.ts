import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ConfirmationService, SelectItem } from 'primeng';
import { Observable, Subscription } from 'rxjs';
import { OrganizacaoService, Organizacao } from '../../organizacao';
import { Sistema } from '../sistema.model';
import { SistemaService, FuncaoDistinta, RenomearFuncao } from '../sistema.service';
import { Modulo } from 'src/app/modulo';
import { Funcionalidade, funcionalidadeRoute, FuncionalidadeService } from 'src/app/funcionalidade';
import { PageNotificationService, DatatableClickEvent } from '@nuvem/primeng-components';
import { BlockUiService } from '@nuvem/angular-base';
import * as FileSaver from 'file-saver';
import * as XLSX from 'xlsx';
import { ExportacaoUtilService } from 'src/app/components/abaco-buttons/export-button/export-button.service';

const EXCEL_TYPE = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8';
const EXCEL_EXTENSION = '.xlsx';

@Component({
    selector: 'app-sistema-form',
    templateUrl: './sistema-form.component.html',
    providers: [ConfirmationService]
})
export class SistemaFormComponent implements OnInit, OnDestroy {

    tiposExportacaoFuncionalidade = [
        {
            label: 'PDF', icon: '', command: () => {
                this.exportar(ExportacaoUtilService.PDF, "funcionalidades");
            }
        },
        {
            label: 'EXCEL', icon: '', command: () => {
                this.exportar(ExportacaoUtilService.EXCEL, "funcionalidades");
            }
        },
        {
            label: 'IMPRIMIR', icon: '', command: () => {
                this.imprimir(ExportacaoUtilService.PDF, "funcionalidades");
            }
        },
    ];
    tiposExportacaoModulo = [
        {
            label: 'PDF', icon: '', command: () => {
                this.exportar(ExportacaoUtilService.PDF, "modulos");
            }
        },
        {
            label: 'EXCEL', icon: '', command: () => {
                this.exportar(ExportacaoUtilService.EXCEL, "modulos");
            }
        },
        {
            label: 'IMPRIMIR', icon: '', command: () => {
                this.imprimir(ExportacaoUtilService.PDF, "modulos");
            }
        },
    ];


    readonly edit = 'edit';
    readonly delete = 'delete';

    organizacoes: Organizacao[] = [];
    sistema: Sistema = new Sistema();
    isSaving; isEdit; boolean;

    tipoSistemaOptions: SelectItem[] = [
        { label: 'Novo', value: 'NOVO' },
        { label: 'Legado', value: 'LEGADO' }
    ];

    mostrarDialogModulo = false;
    mostrarDialogEditarModulo = false;

    listModulos: Modulo[] = [];

    novoModulo: Modulo = new Modulo();
    moduloEmEdicao: Modulo = new Modulo();

    moduloMigracao: Modulo = new Modulo();

    mostrarDialogFuncionalidade = false;
    valido = false;

    mostrarDialogEditarFuncionalidade = false;
    mostrarDialogMigrarFuncionalidade = false;

    novaFuncionalidade: Funcionalidade = new Funcionalidade();
    oldFuncionalidade: Funcionalidade;
    funcionalidadeEmEdicao: Funcionalidade = new Funcionalidade();

    funcionalidadeMigracao: Funcionalidade = new Funcionalidade();

    // Funções de Dados/Transação
    funcoesDistintas: FuncaoDistinta[] = [];
    funcaoSelecionada: FuncaoDistinta = null;
    novoNomeFuncao: string = '';
    novoModuloFuncao: Modulo = null;
    novaFuncionalidadeFuncao: Funcionalidade = null;
    mostrarDialogEditarFuncao = false;
    renomeacoesPendentes: RenomearFuncao[] = [];

    private routeSub: Subscription;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private sistemaService: SistemaService,
        private organizacaoService: OrganizacaoService,
        private confirmationService: ConfirmationService,
        private pageNotificationService: PageNotificationService,
        private funcionalidadeService: FuncionalidadeService,
        private blockUiService: BlockUiService,
    ) {
    }



    ngOnInit() {
        this.isSaving = false;
        this.blockUiService.show();
        this.organizacaoService.dropDownActive().subscribe(response => {
            this.organizacoes = response;
            this.organizacoes.push(new Organizacao());
            this.blockUiService.hide();
        });
        this.routeSub = this.route.params.subscribe(params => {
            if (params['id']) {
                this.blockUiService.show();
                this.sistemaService.find(params['id']).subscribe(
                    sistema => {
                        this.sistema = Sistema.fromJSON(sistema);
                        this.listModulos = Sistema.fromJSON(sistema).modulos;
                        this.blockUiService.hide();
                        // Carregar funções distintas para edição
                        this.carregarFuncoesDistintas();
                    });
            }
        });
    }

    datatableClickModulo(event: DatatableClickEvent) {
        if (!event.selection) {
            return;
        }
        switch (event.button) {
            case this.edit:
                this.moduloEmEdicao = event.selection.clone();
                this.abrirDialogEditarModulo();
                break;
            case this.delete:
                this.moduloEmEdicao = event.selection.clone();
                this.confirmDeleteModulo();
                break;
            default:
                break;
        }
    }
    datatableClickFuncionalidade(event: DatatableClickEvent) {
        if (!event.selection) {
            return;
        }
        switch (event.button) {
            case this.edit:
                this.oldFuncionalidade = event.selection.clone();
                this.funcionalidadeEmEdicao = event.selection.clone();
                this.abrirDialogEditarFuncionalidade();
                break;
            case this.delete:
                this.funcionalidadeEmEdicao = event.selection.clone();
                this.confirmDeleteFuncionalidade();
                break;
            default:
                break;
        }
    }

    abrirDialogEditarModulo() {
        this.mostrarDialogEditarModulo = true;
    }

    fecharDialogEditarModulo() {
        this.moduloEmEdicao = new Modulo();
        this.mostrarDialogEditarModulo = false;
    }

    editarModulo() {
        if (this.moduloEmEdicao.nome === undefined || this.moduloEmEdicao.nome.length === 0) {
            this.valido = true;
            this.pageNotificationService.addErrorMessage('Ocorreu um erro no sistema!');
            return;
        }
        this.valido = false;
        this.sistema.updateModulo(this.moduloEmEdicao);
        this.fecharDialogEditarModulo();
    }

    confirmDeleteModulo() {
        this.confirmationService.confirm({
            message: 'Tem certeza que deseja excluir o módulo ' + this.moduloEmEdicao.nome + ' ?',
            accept: () => {
                if (this.moduleCanBeDeleted()) {
                    this.sistema.deleteModulo(this.moduloEmEdicao);
                    this.moduloEmEdicao = new Modulo();
                } else {
                    this.pageNotificationService.addErrorMessage('O '
                        + this.moduloEmEdicao.nome
                        + ' não pode ser excluído porque existem funcionalidades atribuídas.'
                    );
                }
            }
        });
    }

    private moduleCanBeDeleted() {
        let isDeletationValid = true;

        if (this.sistema.funcionalidades) {
            this.sistema.funcionalidades.forEach(each => {
                if (each.modulo.nome === this.moduloEmEdicao.nome) {
                    isDeletationValid = false;
                }
            });
        }

        return isDeletationValid;
    }

    abrirDialogModulo() {
        this.mostrarDialogModulo = true;
    }

    fecharDialogModulo() {
        this.doFecharDialogModulo();
    }

    private doFecharDialogModulo() {
        this.mostrarDialogModulo = false;
        this.novoModulo = new Modulo();
    }


    adicionarModulo() {
        if (this.novoModulo.nome === undefined) {
            this.valido = true;
            this.pageNotificationService.addErrorMessage('Por favor preencher o campo obrigatório!');
            return;
        }
        for (let i = 0; i < this.sistema?.modulos?.length; i++) {
            const modulo = this.sistema.modulos[i];
            if (modulo.nome.toLocaleLowerCase() === this.novoModulo.nome.toLocaleLowerCase()) {
                this.valido = true;
                return this.pageNotificationService.addErrorMessage('Nome de módulo já existente!');
            }
        }
        this.valido = false;
        this.sistema.addModulo(this.novoModulo);
        this.doFecharDialogModulo();
    }

    deveDesabilitarBotaoNovaFuncionalidade(): boolean {
        return !this.sistema.modulos || this.sistema.modulos.length === 0;
    }

    abrirDialogFuncionalidade() {
        if (!this.deveDesabilitarBotaoNovaFuncionalidade()) {
            this.funcionalidadeEmEdicao.nome = undefined;
            this.funcionalidadeEmEdicao.modulo = undefined;
            this.mostrarDialogFuncionalidade = true;
        }
    }

    fecharDialogFuncionalidade() {
        this.doFecharDialogFuncionalidade();
    }

    private doFecharDialogFuncionalidade() {
        this.mostrarDialogFuncionalidade = false;
        this.novaFuncionalidade = new Funcionalidade();
    }

    adicionarFuncionalidade() {
        if (this.novaFuncionalidade.nome === undefined || this.novaFuncionalidade.modulo === undefined) {
            this.valido = true;
            this.pageNotificationService.addErrorMessage('Por favor preencher o campo obrigatório!');
            return;
        }
        for (let funcionalidade of this.novaFuncionalidade.modulo.funcionalidades) {
            if (funcionalidade.nome.toLocaleLowerCase() === this.novaFuncionalidade.nome.toLocaleLowerCase()) {
                this.valido = true;
                return this.pageNotificationService.addErrorMessage('Nome de funcionalidade já existente!');
            }
        }
        this.valido = false;
        this.sistema.addFuncionalidade(this.novaFuncionalidade);
        this.doFecharDialogFuncionalidade();
    }

    abrirDialogEditarFuncionalidade() {
        this.mostrarDialogEditarFuncionalidade = true;
    }

    fecharDialogEditarFuncionalidade() {
        this.funcionalidadeEmEdicao = new Funcionalidade();
        this.mostrarDialogEditarFuncionalidade = false;
    }

    editarFuncionalidade() {
        // update funciona pois a cópia possui o mesmo artificialId
        if (this.funcionalidadeEmEdicao.nome === undefined || this.funcionalidadeEmEdicao.modulo === undefined || this.funcionalidadeEmEdicao.nome.length === 0) {
            this.valido = true;
            this.pageNotificationService.addErrorMessage('Por favor preencher o campo obrigatório!');
            return;
        }
        this.valido = false;
        if (this.funcionalidadeEmEdicao.modulo.nome !== this.oldFuncionalidade.modulo.nome) {
            this.funcionalidadeService.getTotalFunction(this.oldFuncionalidade.id).subscribe(totalFuncoes => {
                if (totalFuncoes > 0) {
                    return this.pageNotificationService.addErrorMessage('Não é possível editar o módulo da funcionalidade selecionada. Existem funções ligadas a ela.');
                } else {
                    this.sistema.updateFuncionalidade(this.funcionalidadeEmEdicao, this.oldFuncionalidade);
                    this.fecharDialogEditarFuncionalidade();
                }
            })
        } else {
            this.sistema.updateFuncionalidade(this.funcionalidadeEmEdicao, this.oldFuncionalidade);
            this.fecharDialogEditarFuncionalidade();
        }
    }

    confirmDeleteFuncionalidade() {
        if (this.funcionalidadeEmEdicao.id) {
            this.funcionalidadeService.getTotalFunction(this.funcionalidadeEmEdicao.id)
                .subscribe(totalFuncoes => {
                    if (totalFuncoes <= 0) {
                        this.confirmationService.confirm({
                            message: 'Tem certeza que deseja excluir a funcionalidade ' + this.funcionalidadeEmEdicao.nome +
                                ' do módulo ' + this.funcionalidadeEmEdicao.modulo.nome + ' ?',
                            accept: () => {
                                this.sistema.deleteFuncionalidade(this.funcionalidadeEmEdicao);
                                this.moduloEmEdicao = new Modulo();
                            }
                        });
                    } else {
                        this.confirmationService.confirm({
                            message: "Existem funções de dados/transações que ligadas a essa funcionalidade. Você deseja migrar as funções para outra funcionalidade?",
                            accept: () => {
                                this.abrirDialogMigrarFuncionalidade();
                            },
                            reject: () => {
                                this.pageNotificationService.addErrorMessage('Não é possível excluir a funcionalidade selecionada.');
                            }
                        })
                    }
                });
        } else {
            this.confirmationService.confirm({
                message: 'Tem certeza que deseja excluir a funcionalidade ' + this.funcionalidadeEmEdicao.nome +
                    ' do módulo ' + this.funcionalidadeEmEdicao.modulo.nome + ' ?',
                accept: () => {
                    this.sistema.deleteFuncionalidade(this.funcionalidadeEmEdicao);
                    this.moduloEmEdicao = new Modulo();
                }
            });
        }
    }

    save(form) {
        // if (!form.valid) {
        //   this.pageNotificationService.addErrorMessage('Por favor preencher o campo obrigatório!');
        //   return;
        // }
        this.isSaving = true;
        (this.sistema.modulos === undefined) ? (this.sistema.modulos = []) : (this.sistema);
        let sistemas: Array<Sistema>;
        this.sistemaService.dropDown().subscribe(response => {
            sistemas = response;
            if (this.sistema.id !== undefined) {
                (this.checkRequiredFields() && !this.checkDuplicity(sistemas)
                    && this.checkSystemName())
                    ? (this.isEdit = true) && (this.subscribeToSaveResponse(this.sistemaService.update(this.sistema))) : (this);
            } else {
                (this.checkRequiredFields() && !this.checkDuplicity(sistemas)
                    && this.checkSystemName())
                    ? (this.subscribeToSaveResponse(this.sistemaService.create(this.sistema))) : (this);
            }
        });
    }

    private checkDuplicity(sistemas: Array<Sistema>) {
        let isAlreadyRegistered: boolean;

        if (sistemas) {
            sistemas.forEach(each => {
                if (each.nome === this.sistema.nome && each.organizacao.id === this.sistema.organizacao.id && each.id !== this.sistema.id) {
                    isAlreadyRegistered = true;
                    this.pageNotificationService.addErrorMessage('O sistema ' + each.nome + ' já está cadastrado!');
                }
            });
        }
        return isAlreadyRegistered;
    }

    private checkSystemInitials() {
        let exceedsMaximumValue = false;

        if (this.checkIfIsEmpty(this.sistema.sigla)) {
            if (this.sistema.sigla.length >= 255) {
                exceedsMaximumValue = true;
                this.pageNotificationService.addErrorMessage('O campo sigla excede o número de caracteres.');
            }
        }

        return exceedsMaximumValue;
    }

    private checkIfIsEmpty(field: string) {
        let isEmpty = false;

        if (field === undefined || field === null || field === '') {
            isEmpty = true;
        }

        return isEmpty;
    }

    private checkSystemName() {
        let isValid = true;

        if (this.checkIfIsEmpty(this.sistema.nome)) {
            if (this.sistema.nome.length >= 255) {
                isValid = false;
                this.pageNotificationService.addErrorMessage('O campo sigla excede o número de caracteres.');
            }
        }

        return isValid;
    }

    private checkRequiredFields() {
        let isNameValid = false;
        let isTipoValid = false;
        let isInitialsValid = false;
        let isOrganizationValid = false;
        let isRequiredFieldsValid = false;

        this.resetFocusFields();
        (!this.checkIfIsEmpty(this.sistema.nome))
            ? (isNameValid = true)
            : (document.getElementById('nome_sistema').setAttribute('style', 'border-color: red'));
        (!this.checkIfIsEmpty(this.sistema.sigla))
            ? (isInitialsValid = true)
            : (document.getElementById('sigla_sistema').setAttribute('style', 'border-color: red'));

        if (this.sistema.organizacao !== undefined) {
            isOrganizationValid = true;
        } else {
            document.getElementById('organizacao_sistema').setAttribute('style', 'border-bottom: solid; border-bottom-color: red;');
        }

        (this.sistema.tipoSistema) ? isTipoValid = true : isTipoValid = false;

        (isNameValid
            && isInitialsValid
            && isOrganizationValid && isTipoValid)
            ? (isRequiredFieldsValid = true)
            : (isRequiredFieldsValid = false);

        (!isRequiredFieldsValid)
            ? (this.pageNotificationService.addErrorMessage('Favor preencher os campos Obrigatórios!')) : (this);
        return isRequiredFieldsValid;
    }

    private resetFocusFields() {
        document.getElementById('nome_sistema').setAttribute('style', 'border-color: #bdbdbd');
        document.getElementById('sigla_sistema').setAttribute('style', 'border-color: #bdbdbd');
        document.getElementById('organizacao_sistema').setAttribute('style', 'border-bottom: none');
    }

    private notifyRequiredFields() {
        this.pageNotificationService.addErrorMessage('Por favor preencher os campos Obrigatórios!');
        document.getElementById('sigla_sistema').setAttribute('style', 'border-color: red');
    }

    private subscribeToSaveResponse(result: Observable<Sistema>) {
        this.blockUiService.show(); // Mostrar loader
        result.subscribe((res: Sistema) => {
            // Alterado: Diferenciar comportamento entre criação e edição
            if (this.isEdit) {
                // Modo edição: Primeiro aplicar renomeações pendentes, se houver
                if (this.renomeacoesPendentes.length > 0) {
                    console.log('[SistemaForm] Aplicando renomeações pendentes:', this.renomeacoesPendentes);
                    this.sistemaService.renomearFuncoes(res.id, this.renomeacoesPendentes).subscribe(
                        (totalRenomeadas) => {
                            console.log('[SistemaForm] Funções renomeadas:', totalRenomeadas);
                            this.renomeacoesPendentes = []; // Limpar lista de pendentes
                            this.isSaving = false;
                            this.sistema = Sistema.fromJSON(res);
                            this.listModulos = this.sistema.modulos;
                            this.carregarFuncoesDistintas(); // Recarregar funções para refletir as mudanças
                            this.blockUiService.hide(); // Esconder loader
                            this.pageNotificationService.addUpdateMsg();
                        },
                        (error) => {
                            console.error('[SistemaForm] Erro ao renomear funções:', error);
                            this.isSaving = false;
                            this.blockUiService.hide(); // Esconder loader
                            this.pageNotificationService.addErrorMessage('Erro ao renomear funções. O sistema foi salvo, mas as renomeações não foram aplicadas.');
                        }
                    );
                } else {
                    // Sem renomeações pendentes, apenas atualizar dados locais
                    this.isSaving = false;
                    this.sistema = Sistema.fromJSON(res);
                    this.listModulos = this.sistema.modulos;
                    this.blockUiService.hide(); // Esconder loader
                    this.pageNotificationService.addUpdateMsg();
                }
            } else {
                // Modo criação: navegar para tela de edição com novo ID
                this.isSaving = false;
                this.blockUiService.hide(); // Esconder loader
                this.pageNotificationService.addCreateMsg('Sistema cadastrado com sucesso!');
                this.router.navigate(['/sistema', res.id, 'edit']);
            }
        }, (error: Response) => {
            this.isSaving = false;
            this.blockUiService.hide(); // Esconder loader

            switch (error.status) {
                case 404: {
                    this.pageNotificationService.addErrorMessage('Campos inválidos: ' + error['body']);
                    break;
                }
                default: {
                    this.pageNotificationService.addErrorMessage('Ocorreu um erro no sistema!');
                    break;
                }
            }
        });
    }


    abrirDialogMigrarFuncionalidade() {
        this.mostrarDialogMigrarFuncionalidade = true;
    }

    fecharDialogMigrarFuncionalidade() {
        this.mostrarDialogMigrarFuncionalidade = false;
    }

    migrarFuncoes() {
        if (this.funcionalidadeMigracao == null || this.funcionalidadeMigracao == undefined) {
            return this.pageNotificationService.addErrorMessage("Escolha uma funcionalidade para fazer a migração.");
        }
        if (this.funcionalidadeEmEdicao.id === this.funcionalidadeMigracao.id) {
            return this.pageNotificationService.addErrorMessage("Você não pode migrar para a funcionalidade que irá excluir.");
        }
        if (this.funcionalidadeMigracao.id == undefined || this.funcionalidadeMigracao.id == null) {
            return this.pageNotificationService.addErrorMessage("Escolha uma funcionalidade salva para fazer a migração.");
        }
        this.funcionalidadeService.migrarFuncoes(this.funcionalidadeEmEdicao.id, this.funcionalidadeMigracao.id).subscribe(response => {
            this.sistema.deleteFuncionalidade(this.funcionalidadeEmEdicao);
            this.pageNotificationService.addSuccessMessage("Migração de funções concluída!");
            this.fecharDialogMigrarFuncionalidade();
        }, error => { this.pageNotificationService.addErrorMessage("Erro: Migração de funções não concluída.") });

    }

    mudarModulo(modulo) {
        this.moduloMigracao = modulo.value;
        this.funcionalidadeMigracao = null;
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }

    exportar(tipoRelatorio: string, resourceName: string) {
        let funcs: any[] = [];
        if (resourceName == "modulos") {
            this.sistema.modulos.forEach(modulo => {
                funcs.push(new Modulo(modulo.id, modulo.nome));
            });
        } else if (resourceName == "funcionalidades") {
            this.sistema.funcionalidades.forEach(fnc => {
                funcs.push(new Funcionalidade(fnc.id, fnc.nome, new Modulo(fnc.modulo.id, fnc.modulo.nome)));
            });
        }
        this.sistemaService.exportar(tipoRelatorio, funcs, resourceName);

    }

    imprimir(tipoRelatorio: string, resourceName: string) {
        let funcs: any[] = [];
        if (resourceName == "modulos") {
            this.sistema.modulos.forEach(modulo => {
                funcs.push(new Modulo(modulo.id, modulo.nome));
            });
        } else if (resourceName == "funcionalidades") {
            this.sistema.funcionalidades.forEach(fnc => {
                funcs.push(new Funcionalidade(fnc.id, fnc.nome, new Modulo(fnc.modulo.id, fnc.modulo.nome)));
            });
        }
        this.sistemaService.imprimir(funcs, resourceName);

    }

    /**
     * Carrega todas as funções distintas (combinação única de Módulo, Funcionalidade e Nome)
     * de todas as análises associadas ao sistema.
     */
    carregarFuncoesDistintas() {
        if (this.sistema && this.sistema.id) {
            console.log('[SistemaForm] Carregando funções distintas para sistema:', this.sistema.id);
            this.sistemaService.getFuncoesDistintas(this.sistema.id).subscribe(
                (funcoes) => {
                    // Ordena por módulo, funcionalidade e nome e adiciona ID único
                    this.funcoesDistintas = (funcoes || [])
                        .sort((a, b) => {
                            const comp1 = (a.nomeModulo || '').localeCompare(b.nomeModulo || '');
                            if (comp1 !== 0) return comp1;
                            const comp2 = (a.nomeFuncionalidade || '').localeCompare(b.nomeFuncionalidade || '');
                            if (comp2 !== 0) return comp2;
                            return (a.nomeFuncao || '').localeCompare(b.nomeFuncao || '');
                        })
                        .map((f, index) => ({
                            ...f,
                            id: `${f.nomeModulo}_${f.nomeFuncionalidade}_${f.nomeFuncao}_${index}`
                        }));
                    console.log('[SistemaForm] Funções distintas carregadas:', this.funcoesDistintas.length);
                },
                (error) => {
                    console.error('[SistemaForm] Erro ao carregar funções distintas:', error);
                }
            );
        }
    }

    /**
     * Gerencia o clique na tabela de funções distintas.
     */
    datatableClickFuncaoDistinta(event: DatatableClickEvent) {
        if (!event.selection) {
            return;
        }
        switch (event.button) {
            case this.edit:
                this.funcaoSelecionada = event.selection;
                this.abrirDialogEditarFuncao();
                break;
            default:
                break;
        }
    }

    /**
     * Abre o dialog para editar uma função (nome, módulo ou funcionalidade).
     */
    abrirDialogEditarFuncao() {
        if (this.funcaoSelecionada) {
            this.novoNomeFuncao = this.funcaoSelecionada.nomeFuncao;
            // Buscar o módulo atual
            this.novoModuloFuncao = this.listModulos.find(m => m.nome === this.funcaoSelecionada.nomeModulo) || null;
            // Buscar a funcionalidade atual
            if (this.novoModuloFuncao) {
                this.novaFuncionalidadeFuncao = this.novoModuloFuncao.funcionalidades?.find(
                    f => f.nome === this.funcaoSelecionada.nomeFuncionalidade
                ) || null;
            } else {
                this.novaFuncionalidadeFuncao = null;
            }
            this.mostrarDialogEditarFuncao = true;
        } else {
            this.pageNotificationService.addErrorMessage('Selecione uma função para editar.');
        }
    }

    /**
     * Fecha o dialog de edição de função.
     */
    fecharDialogEditarFuncao() {
        this.mostrarDialogEditarFuncao = false;
        this.funcaoSelecionada = null;
        this.novoNomeFuncao = '';
        this.novoModuloFuncao = null;
        this.novaFuncionalidadeFuncao = null;
    }

    /**
     * Retorna as funcionalidades do módulo selecionado no combo de novo módulo.
     */
    getFuncionalidadesDoNovoModulo(): Funcionalidade[] {
        return this.novoModuloFuncao?.funcionalidades || [];
    }

    /**
     * Handler para quando o módulo muda no combo de novo módulo.
     * Atualiza a funcionalidade para a primeira do novo módulo ou null.
     */
    onNovoModuloChange() {
        if (this.novoModuloFuncao && this.novoModuloFuncao.funcionalidades?.length > 0) {
            // Tentar manter a funcionalidade atual se existir no novo módulo
            const funcExistente = this.novoModuloFuncao.funcionalidades.find(
                f => f.nome === this.funcaoSelecionada.nomeFuncionalidade
            );
            this.novaFuncionalidadeFuncao = funcExistente || this.novoModuloFuncao.funcionalidades[0];
        } else {
            this.novaFuncionalidadeFuncao = null;
        }
    }

    /**
     * Salva a alteração da função.
     * Adiciona à lista de alterações pendentes e atualiza a tabela local.
     */
    salvarRenomeacaoFuncao() {
        if (!this.funcaoSelecionada) {
            this.pageNotificationService.addErrorMessage('Nenhuma função selecionada.');
            return;
        }

        // Verificar quais campos foram alterados
        const novoNomeTrimmed = this.novoNomeFuncao?.trim() || '';
        const nomeAlterado = novoNomeTrimmed !== '' && novoNomeTrimmed !== this.funcaoSelecionada.nomeFuncao;
        const moduloAlterado = this.novoModuloFuncao && this.novoModuloFuncao.nome !== this.funcaoSelecionada.nomeModulo;
        const funcionalidadeAlterada = this.novaFuncionalidadeFuncao && this.novaFuncionalidadeFuncao.nome !== this.funcaoSelecionada.nomeFuncionalidade;

        // Se nenhum campo foi alterado, exibir mensagem e manter modal aberta
        if (!nomeAlterado && !moduloAlterado && !funcionalidadeAlterada) {
            this.pageNotificationService.addErrorMessage('Nenhum dado da função foi alterado.');
            return;
        }

        // Adicionar à lista de alterações pendentes
        const renomeacao: RenomearFuncao = {
            nomeModulo: this.funcaoSelecionada.nomeModulo,
            nomeFuncionalidade: this.funcaoSelecionada.nomeFuncionalidade,
            nomeAtual: this.funcaoSelecionada.nomeFuncao,
            novoNome: nomeAlterado ? novoNomeTrimmed : null,
            novoModulo: moduloAlterado ? this.novoModuloFuncao.nome : null,
            novaFuncionalidade: funcionalidadeAlterada ? this.novaFuncionalidadeFuncao.nome : null
        };
        this.renomeacoesPendentes.push(renomeacao);
        console.log('[SistemaForm] Alteração pendente adicionada:', renomeacao);

        // Atualizar a tabela local
        const index = this.funcoesDistintas.findIndex(f =>
            f.nomeModulo === this.funcaoSelecionada.nomeModulo &&
            f.nomeFuncionalidade === this.funcaoSelecionada.nomeFuncionalidade &&
            f.nomeFuncao === this.funcaoSelecionada.nomeFuncao
        );
        if (index !== -1) {
            if (nomeAlterado) {
                this.funcoesDistintas[index].nomeFuncao = novoNomeTrimmed;
            }
            if (moduloAlterado) {
                this.funcoesDistintas[index].nomeModulo = this.novoModuloFuncao.nome;
            }
            if (funcionalidadeAlterada) {
                this.funcoesDistintas[index].nomeFuncionalidade = this.novaFuncionalidadeFuncao.nome;
            }
        }

        this.pageNotificationService.addSuccessMessage('Alteração registrada. Salve o sistema para aplicar.');
        this.fecharDialogEditarFuncao();
    }

    /**
     * Verifica se o botão de editar função deve estar desabilitado.
     */
    deveDesabilitarBotaoEditarFuncao(): boolean {
        return !this.funcaoSelecionada;
    }
}
