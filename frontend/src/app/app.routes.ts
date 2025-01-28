import {Routes} from '@angular/router';
import {HomeComponent} from './components/home/home.component';
import {RegistrationComponent} from './components/registration/registration.component';
import {LoginComponent} from './components/login/login.component';
import {AuthGuard} from './guards/auth.guard';
import {RedirectGuard} from './guards/redirect.guard';
import {DashboardComponent} from './components/dashboard/dashboard.component';
import {ProfilePageComponent} from './components/profile-page/profile-page.component';
import {UnauthenticatedOnlyGuard} from './guards/unauthenticated.only.guard';
import {AddProductComponent} from './components/add-product/add-product.component';
import {CreateCompetitorComponent} from './components/create-competitor/create-competitor.component';
import {ProductsComponent} from './components/products/products.component';
import {ProductDetailsComponent} from './components/product-details/product-details.component';
import {ForgotPasswordComponent} from './components/forgot-password/forgot-password.component';
import {ChangePasswordComponent} from './components/change-password/change-password.component';
import {DynamicPricingPageComponent} from './components/dynamic-pricing-page/dynamic-pricing-page.component';
import {PriceRulePageComponent} from './components/price-rule-page/price-rule-page.component';
import {PriceRuleFormComponent} from './components/price-rule-form/price-rule-form.component';
import {ApiAccessComponent} from './components/profile-page/api-access/api-access.component';
import {ApiDocsComponent} from './components/profile-page/api-access/api-docs/api-docs.component';
import {CompetitorsComponent} from './components/competitors/competitors.component';


export const routes: Routes = [
  {path: '', component: HomeComponent, canActivate: [RedirectGuard]},
  {path: 'registration', component: RegistrationComponent, canActivate: [UnauthenticatedOnlyGuard]},
  {path: 'login', component: LoginComponent, canActivate: [UnauthenticatedOnlyGuard]},
  {path: 'forgot-password', component: ForgotPasswordComponent, canActivate: [UnauthenticatedOnlyGuard]},
  {path: 'change-password', component: ChangePasswordComponent, canActivate: [UnauthenticatedOnlyGuard]},
  {path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard]},
  // Dynamic Pricing Routes
  {path: 'dynamic-pricing', component: DynamicPricingPageComponent, canActivate: [AuthGuard]},
  {path: 'dynamic-pricing/rules/create', component: PriceRuleFormComponent, canActivate: [AuthGuard]},
  {path: 'dynamic-pricing/rules/:id', component: PriceRuleFormComponent, canActivate: [AuthGuard]},
  {path: 'rule', component: PriceRulePageComponent, canActivate: [AuthGuard]},
  {path: 'create-competitor', component: CreateCompetitorComponent, canActivate: [AuthGuard]},
  {path: 'products', component: ProductsComponent, canActivate: [AuthGuard]},
  {path: 'api-access', component: ApiAccessComponent, canActivate: [AuthGuard]},
  {path: 'competitors', component: CompetitorsComponent, canActivate: [AuthGuard]},
  {
    path: ':id/profile', canActivate: [AuthGuard], children:
      [
        {
          path: 'details',
          component: ProfilePageComponent,
        }
      ]
  },
  {path: 'add-product', component: AddProductComponent, canActivate: [AuthGuard]},
  {
    path: ':productId/product', canActivate: [AuthGuard], children:
      [
        {
          path: 'details',
          component: ProductDetailsComponent,
        }
      ]
  },
  {path: '**', redirectTo: ''},
];
