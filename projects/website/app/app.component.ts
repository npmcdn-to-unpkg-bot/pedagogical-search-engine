/*/ Patch the angular2 router issue with ctrl+click
// see: https://github.com/angular/angular/issues/5908
import {Directive} from 'angular2/core';
import {Router, RouterLink, Location, ROUTER_DIRECTIVES} from 'angular2/router';

@Directive({
    selector: '[routerLink]',
    inputs: ['routeParams: routerLink', 'target: target'],
    host: {
        '(click)': 'onClickWithEvent($event)',
        '[attr.href]': 'visibleHref',
        '[class.router-link-active]': 'isRouteActive'
    }
})
export class MiddleClickRouterLink extends RouterLink {
    constructor(router: Router, location: Location) {
        super(router, location);
    }
    public onClickWithEvent(e: MouseEvent): any {
        if (e.ctrlKey || e.metaKey || e.button === 1) {
            return true;
        }
        return super.onClick();
    }
}

// Override Angular's RouterLink with our patched version
ROUTER_DIRECTIVES[1] = MiddleClickRouterLink;
*/


// Normal App component
import {Component, provide} from "angular2/core";
import {RouteConfig, ROUTER_DIRECTIVES} from "angular2/router";
import {SearchCmp} from "./search/search.component";
import {ClassificationService} from "./search/user-study/classification.service";
import {SimpleClassificationService} from "./search/user-study/simple-classification.service";

@Component({
    selector: 'wc-app',
    template: `

<router-outlet></router-outlet>

`,
    directives: [ROUTER_DIRECTIVES],
    providers: [
        provide(ClassificationService, {useClass: SimpleClassificationService})
    ]
})
@RouteConfig([
    {path:'/search', name: 'Search', component: SearchCmp, useAsDefault: true}
])
export class AppComponent {
}


