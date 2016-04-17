import {Component} from "angular2/core";
import {RouteConfig, ROUTER_DIRECTIVES} from "angular2/router";
import {SearchCmp} from "./search/search.component";

@Component({
    selector: 'wc-app',
    template: `

<h1>WikiChimp</h1> 
<router-outlet></router-outlet>

`,
    directives: [ROUTER_DIRECTIVES]
})
@RouteConfig([
    {path:'/search', name: 'Search', component: SearchCmp, useAsDefault: true}
])
export class AppComponent {
}

