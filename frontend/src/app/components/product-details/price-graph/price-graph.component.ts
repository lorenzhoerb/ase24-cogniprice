import {Component, ElementRef, Input, OnChanges, SimpleChanges} from '@angular/core';
import {Chart, ChartConfiguration, ChartData, ChartType, registerables} from 'chart.js';
import {MatButtonModule} from '@angular/material/button';
import {BaseChartDirective} from 'ng2-charts';
import 'chartjs-adapter-date-fns';
import {PriceService} from '../../../services/price/price.service';
import {NgIf} from '@angular/common';

Chart.register(...registerables);

@Component({
  selector: 'app-price-graph',
  standalone: true,
  imports: [
    BaseChartDirective,
    MatButtonModule,
    NgIf
  ],
  templateUrl: './price-graph.component.html',
  styleUrl: './price-graph.component.scss'
})
export class PriceGraphComponent implements OnChanges {

  @Input() productId: number | null = null;

  private resizeObserver: ResizeObserver | null = null;

  private fullPriceHistory: any[] = []; // Store the full price history
  private filteredPriceHistory: any[] = []; // Store the filtered price history
  private colorMap: { [key: string]: string } = {}; // Map competitor names to colors
  private lineTension: number = 0;

  constructor(private priceService: PriceService, private elementRef: ElementRef) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['productId'] && changes['productId'].currentValue) {
      this.getPricesforProductId(this.productId!);
    }
  }

  getPricesforProductId(productId: number) {
    this.priceService.getPriceHistoryByProductId(productId).subscribe({
      next: (priceHistory) => {
        this.fullPriceHistory = priceHistory;
        this.filterByTime('ALL');
      },
      error: (err) => {
        console.log('Error fetching price history: ', err);
      }
    })
  }

  public lineChartData: ChartData<'line'> = { datasets: [] };
  public lineChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: {
        position: 'top',
        labels: {
          font: {
            size: this.getDynamicFontSize(), // Call the function to get dynamic font size
          },
        },
      },
      tooltip: {
        mode: 'index',
        intersect: false,
        bodyFont: {
          size: this.getDynamicFontSize(),
        },
        titleFont: {
          size: this.getDynamicFontSize(),
        },
      },
    },
    scales: {
      x: {
        type: 'time',
        time: {
          unit: 'day',
        },
        title: {
          display: true,
          text: 'Date',
          font: {
            size: this.getDynamicFontSize(),
          },
        },
        ticks: {
          font: {
            size: this.getDynamicFontSize(),
          },
        },
      },
      y: {
        beginAtZero: false,
        title: {
          display: true,
          text: 'Price (EUR)',
          font: {
            size: this.getDynamicFontSize(),
          },
        },
        ticks: {
          font: {
            size: this.getDynamicFontSize(),
          },
        },
      },
    },
  };
  private getDynamicFontSize(): number {
    const width = window.innerWidth;
    if (width > 1200) {
      return 18; // Larger font for large screens
    } else if (width > 768) {
      return 16; // Medium font for tablets
    } else {
      return 14; // Smaller font for small screens
    }
  }
  public lineChartType: ChartType = 'line';

  private processData(priceHistory: any[]) {
    if (priceHistory.length === 0) {
      this.lineChartData = { datasets: [] };
      return;
    }
    const groupedData = this.groupByCompetitorName(priceHistory);
    console.log("Grouped Data: " + groupedData);
    const datasets = Object.keys(groupedData).map((competitorName, index) => ({
      label: competitorName,
      data: groupedData[competitorName].map((entry) => ({
        x: new Date(entry.priceTime).getTime(),
        y: entry.price,
      })),
      borderColor: this.getColorForCompetitor(competitorName),
      fill: false,
      tension: this.lineTension,
      hidden: index >= 3, // Hide all datasets after the first three
    }));

    this.lineChartData = { datasets };
  }

  filterByTime(range: '1M' | '3M' | '6M' | '1Y' | 'ALL'): void {
    const now = new Date();
    let startDate: Date | null = null;

    switch (range) {
      case '1M':
        startDate = new Date();
        startDate.setMonth(now.getMonth() - 1);
        break;
      case '3M':
        startDate = new Date();
        startDate.setMonth(now.getMonth() - 3);
        break;
      case '6M':
        startDate = new Date();
        startDate.setMonth(now.getMonth() - 6);
        break;
      case '1Y':
        startDate = new Date();
        startDate.setFullYear(now.getFullYear() - 1);
        break;
      case 'ALL':
        startDate = null; // No filtering, show all data
        break;
    }

    if (startDate) {
      this.filteredPriceHistory = this.fullPriceHistory.filter((entry) =>
        new Date(entry.priceTime) >= startDate
      );
    } else {
      this.filteredPriceHistory = [...this.fullPriceHistory]; // Show all data
    }

    this.processData(this.filteredPriceHistory);
  }

  private getColorForCompetitor(competitorName: string): string {
    if (!this.colorMap[competitorName]) {
      this.colorMap[competitorName] = this.getRandomColor();
    }
    return this.colorMap[competitorName];
  }

  private groupByCompetitorName(data: any[]): { [key: string]: any[] } {
    return data.reduce((acc, curr) => {
      acc[curr.competitorName] = acc[curr.competitorName] || [];
      acc[curr.competitorName].push(curr);
      return acc;
    }, {});
  }

  private getRandomColor(): string {
    return `#${Math.floor(Math.random() * 16777215).toString(16)}`;
  }


  /* Resize checks but might need fix, as only when getting bigger, onyl refresh makes it ok */
  private initResizeObserver(): void {
    this.resizeObserver = new ResizeObserver(() => {
      const chart = this.elementRef.nativeElement.querySelector('canvas');
      if (chart) {
        const chartInstance = Chart.getChart(chart); // Get the chart instance
        if (chartInstance) {
          chartInstance.resize(); // Trigger a resize of the chart
        }
      }
    });

    this.resizeObserver.observe(this.elementRef.nativeElement); // Observe the container
  }

  private destroyResizeObserver(): void {
    if (this.resizeObserver) {
      this.resizeObserver.disconnect(); // Stop observing
      this.resizeObserver = null;
    }
  }
}
