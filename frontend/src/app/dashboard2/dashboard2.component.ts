import {Component, OnInit, AfterViewInit, ViewChild} from '@angular/core';
import {Dashboard2Service} from "./dashboard2.service";
import {UIChart} from "primeng";

@Component({
    selector: 'app-dashboard2',
    templateUrl: './dashboard2.component.html',
    styleUrls: ['./dashboard2.component.css']
})
export class Dashboard2Component implements OnInit, AfterViewInit{
    motivoData: any;
    motivoOptions: any;
    clienteData: any;
    clienteOptions: any;
    historicoDiferencaData: any;
    historicoDiferencaOptions: any;
    motivosNomes: string[] = [];
    motivosNumeros: number[] = [];
    clientes: string[] = [];
    clientesNumeros: number[] = [];
    diferencaOriginalAjustado: number[] = [];
    datasDemandas: string[] = [];
    totalDemandas: number;
    pfDiferencaGlobal: number;
    @ViewChild("motivoChart") motivoChart: UIChart;
    @ViewChild("clienteChart") clienteChart: UIChart;
    @ViewChild("historicoChart") historicoChart: UIChart;

    constructor(private dashboard2Service : Dashboard2Service) {
    }

    ngOnInit(): void {
        this.motivoData = {
            labels: this.motivosNomes,
            datasets: [
                {
                    data: this.motivosNumeros,
                    backgroundColor: [
                        "#42A5F5",
                        "#66BB6A",
                        "#FFA726",
                        "#FF0000",
                        "#FF7C08"
                    ],
                    hoverBackgroundColor: [
                        "#64B5F6",
                        "#81C784",
                        "#FFB74D",
                        "#FF5959",
                        "#FFA14D"
                    ]
                }
            ]
        };
        this.motivoOptions = {
            responsive:false,
            maintainAspectRatio: false,
            title:{
                display: true,
                text:'Motivo',
                fontFamily: 'Titillium Web, sans-serif',
            },
            tooltips: {
                callbacks: {
                    label: function (tooltipItem, data) {
                        let allData = data.datasets[tooltipItem.datasetIndex].data;
                        let tooltipLabel = data.labels[tooltipItem.index];
                        let tooltipData = allData[tooltipItem.index];
                        let total = 0;
                        for (let i in allData) {
                            total += allData[i];
                        }
                        let tooltipPercentage = Math.round((tooltipData / total) * 100);
                        return tooltipLabel + ': ' + tooltipData + ' (' + tooltipPercentage + '%)';
                    }
                }
            },
            legend: {
                position: 'right',
                labels: {
                    usePointStyle: true,
                    fontSize: 10,
                    fontFamily: 'Titillium Web, sans-serif'
                }
            }
        };

        this.clienteData = {
            labels: this.clientes,
            fontFamily: 'font-family: Titillium Web, sans-serif',
            datasets: [
                {
                    data: this.clientesNumeros,
                    backgroundColor: [
                        "#42A5F5",
                        "#66BB6A",
                        "#FFA726",
                        "#FF0000",
                        "#FF7C08",
                        "#D6FF1C",
                        "#02CD18",
                        "#03E9D8",
                        "#9C18F8",
                        "#FF00DC",
                        "#6C343E",
                        "#4F4670",
                        "#757575",
                        "#48056A",
                        "#1375A2",
                        "#DDAE58",
                        "#4D6F4E",
                        "#A47A56",
                        "#ABB691",
                        "#D9AEFF",
                        "#87B2C5",
                        "#C6AAAA",
                        "#000000",
                        "#F46458",
                        "#F4EE77",
                        "#AAC1FF"
                    ],
                    hoverBackgroundColor: [
                        "#64B5F6",
                        "#81C784",
                        "#FFB74D",
                        "#FF5959",
                        "#FFA14D",
                        "#E4FF6A",
                        "#4FCA5C",
                        "#7DE5DE",
                        "#B558F7",
                        "#FE77EB",
                        "#7E535B",
                        "#5F5A73",
                        "#9E9E9E",
                        "#714686",
                        "#4E95B6",
                        "#EAC27A",
                        "#6C8B6D",
                        "#C6A07F",
                        "#CFD8B9",
                        "#E9D2FE",
                        "#9FC5D6",
                        "#DBC4C4",
                        "#3E3E3E",
                        "#FC8E85",
                        "#FFFBA7",
                        "#CAD9FF"
                    ]
                }
            ]
        };
        this.clienteOptions = {
            responsive: false,
            maintainAspectRatio:false,
            title:{
                display: true,
                text:'Cliente',
                fontFamily: 'Titillium Web, sans-serif'

            },
            tooltips: {
                callbacks: {
                    label: function (tooltipItem, data) {
                        let allData = data.datasets[tooltipItem.datasetIndex].data;
                        let tooltipLabel = data.labels[tooltipItem.index];
                        let tooltipData = allData[tooltipItem.index];
                        let total = 0;
                        for (let i in allData) {
                            total += allData[i];
                        }
                        let tooltipPercentage = Math.round((tooltipData / total) * 100);
                        return tooltipLabel + ': ' + tooltipData + ' (' + tooltipPercentage + '%)';
                    }
                }
            },
            legend: {
                position: 'right',
                labels: {
                    usePointStyle: true,
                    fontSize:10,
                    fontFamily: 'Titillium Web, sans-serif'
                }
            }
        }
        this.historicoDiferencaData = {
            labels: this.datasDemandas,
            datasets: [
                {
                    label: 'Histórico Diferença',
                    fontFamily: 'Titillium Web, sans-serif',
                    data: this.diferencaOriginalAjustado,
                    fill: false,
                    borderColor:'#007FFF',
                    tension: .4
                }
            ]
        };
        this.historicoDiferencaOptions = {
            legend: {
                labels: {
                    color: '#495057',
                    fontFamily: 'Titillium Web, sans-serif'
                }
            },
            scales: {
                x: {
                    ticks: {
                        color: '#495057'
                    },
                    grid: {
                        color: '#ebedef'
                    }
                },
                y: {
                    ticks: {
                        color: '#495057'
                    },
                    grid: {
                        color: '#ebedef'
                    }
                }
            }
        };
    };

    ngAfterViewInit() {
        this.dashboard2Service.ObterMotivos().subscribe(analise => {
            analise.forEach(m => {
                this.motivosNomes.push(m.motivoAnalise);
                this.motivosNumeros.push(m.contaMotivo);
                this.motivoChart.refresh();
            })
        });

        this.dashboard2Service.ObterClientes().subscribe(analise => {
            analise.forEach(mo => {
                this.clientes.push(mo.cliente);
                this.clientesNumeros.push(mo.contaCliente);
                this.clienteChart.refresh();
            })
        })

        this.dashboard2Service.ObterDiferencaPf().subscribe(analise => {
            analise.forEach(dp => {
                this.diferencaOriginalAjustado.push(dp.pfDiferenca);
                this.datasDemandas.push(dp.dataDemanda);
                this.historicoChart.refresh();
            })
        })

        this.dashboard2Service.ObterTotalDemandas().subscribe(analise => {
            analise.forEach(td => {
                this.totalDemandas = td.totalDemandas;
            })
        })

        this.dashboard2Service.ObterPfDiferencaGlobal().subscribe(analise => {
            analise.forEach(dg => {
                this.pfDiferencaGlobal = dg.pfDiferencaGlobal;
            })
        })
    }
}

