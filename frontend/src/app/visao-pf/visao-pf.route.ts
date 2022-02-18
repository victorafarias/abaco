
import { AuthGuard } from '@nuvem/angular-base';
import { Routes } from '@angular/router';
import { FuncaoDadosVisaoPfComponent } from '../funcao-dados-visao-pf/funcao-dados-visao-pf.component';
import { FuncaoTransacaoVisaoPfComponent } from '../funcao-transacao-visao-pf/funcao-transacao-visao-pf.component';


export const visaopfRoute: Routes = [
    
    { path: 'visaopf/:id/deteccomponentes/funcao-dados', component: FuncaoDadosVisaoPfComponent, canActivate: [AuthGuard] },

    { path: 'visaopf/deteccomponentes/funcao-dados/:idTela', component: FuncaoDadosVisaoPfComponent },

    { path: 'visaopf/:id/deteccomponentes/funcao-transacao', component: FuncaoTransacaoVisaoPfComponent, canActivate: [AuthGuard] },

    { path: 'visaopf/deteccomponentes/funcao-transacao/:idTela', component: FuncaoTransacaoVisaoPfComponent },
];
