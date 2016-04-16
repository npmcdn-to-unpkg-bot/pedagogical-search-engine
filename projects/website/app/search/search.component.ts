import {Component} from "angular2/core";
import {RouteConfig, ROUTER_DIRECTIVES} from "angular2/router";
import {SearchPageCmp} from "./search-page.component";

@Component({
    template: `
    
    <router-outlet></router-outlet>
    
    `,
    directives: [ROUTER_DIRECTIVES]
})
@RouteConfig([
    {path: '/', name: 'SearchPage', component: SearchPageCmp, useAsDefault: true}
])
export class SearchCmp {

}