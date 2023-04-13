import {Routes} from '@angular/router';
import {AdministracaoComponent} from "./administracao.component";

export const administracaoRoutes: Routes = [
	{
		path: 'administracao',
		component: AdministracaoComponent,
		data: {breadcrumb: "Administração"}
	}
];
