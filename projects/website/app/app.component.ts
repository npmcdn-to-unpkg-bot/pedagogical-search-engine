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
import {AboutCmp} from "./about/about.component";
import {HowItWorksCmp} from "./about/howitworks.component";
import {FeedbackCmp} from "./feedback/feedback.component";
import {UserstudyService} from "./userstudy/userstudy";
import {MessageService} from "./message.service";
import {FeedbackService} from "./feedback/feedback.service";
import {LocalStorageService} from "./utils/LocalStorageEmitter";
import {HelperService} from "./helper/helper.service";

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
            Experimental search engine for students
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
                <a [routerLink]="['Feedback']">
                    Give Feedback
                </a>
            </span>
            
            &#187;
            
            <span>
                <a [routerLink]="['HowItWorks']">
                    How it works
                </a>
            </span>
            
            &#187;
            
            <span>
                <a [routerLink]="['About']">
                    About
                </a>
            </span>
        </div>
       
    </div>
</div>

`,
    directives: [ROUTER_DIRECTIVES],
    providers: [
        provide(ClassificationService, {useClass: SimpleClassificationService}),
        provide(UserstudyService, {useClass: UserstudyService}),
        provide(MessageService, {useClass: MessageService}),
        provide(FeedbackService, {useClass: FeedbackService}),
        LocalStorageService,
        HelperService
    ]
})
@RouteConfig([
    {path:'/search', name: 'Search', component: SearchCmp, useAsDefault: true},
    {path:'/about', name: 'About', component: AboutCmp},
    {path:'/howitworks', name: 'HowItWorks', component: HowItWorksCmp},
    {path:'/feedback', name: 'Feedback', component: FeedbackCmp}
])
export class AppComponent {
    private _year = new Date().getFullYear();
    private _month = new Date().getMonth();
}


