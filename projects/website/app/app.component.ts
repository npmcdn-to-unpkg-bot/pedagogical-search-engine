import {Component} from "angular2/core";
import {RouteConfig, ROUTER_DIRECTIVES} from "angular2/router";
import {HomepageCmp} from "./homepage.component";
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
    {path:'/', name: 'HomePage', component: HomepageCmp},
    {path:'/search', name: 'Search', component: SearchCmp}
])
export class AppComponent {
}

