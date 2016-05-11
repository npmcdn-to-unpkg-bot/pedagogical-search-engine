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

<div class="wc-com-app-container">
    <div class="wc-com-app-header">
        <a [routerLink]="['Search']">
            <span class="wc-com-app-header-title">
                Wikichimp
            </span>
        </a>
        <span class="wc-com-app-header-text">
            Search engine for students
        </span>
    </div>
    
    <div class="wc-com-app-content">
        <router-outlet></router-outlet>
    </div>
    
    <div class="wc-com-app-footer">
        <div class="wc-com-app-footer-left">
            <span class="wc-com-app-footer-logo">
                Wikichimp 
            </span>
            
            <span class="wc-com-app-footer-date">
                {{ _year + "." + _month }}
            </span>
        </div>
        
        <div class="wc-com-app-footer-right">
            <span>
                <a>
                    Give us Feedback
                </a>
            </span>
            
            &#187;
            
            <span>
                <a>
                    Privacy
                </a>
            </span>
            
            &#187;
            
            <span>
                <a>
                    How it works
                </a>
            </span>
        </div>
       
    </div>
</div>

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
    private _year = new Date().getFullYear();
    private _month = new Date().getMonth();
}


