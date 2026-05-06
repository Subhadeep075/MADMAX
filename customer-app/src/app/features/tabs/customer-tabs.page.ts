import { Component } from '@angular/core';
import { IONIC_STANDALONE_IMPORTS } from '../../shared/ionic-standalone-imports';

@Component({
  standalone: true,
  selector: 'app-customer-tabs',
  imports: [...IONIC_STANDALONE_IMPORTS],
  templateUrl: './customer-tabs.page.html'
})
export class CustomerTabsPage {}
