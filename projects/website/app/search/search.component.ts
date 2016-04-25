import {Component, provide, ChangeDetectorRef} from "angular2/core";
import {SearchBarCmp} from "./bar/bar.component";
import {ResultsCmp} from "./results/results.component";
import {FactoryService} from "./search-terms/factory.service";
import {RouteParams, Router, ROUTER_DIRECTIVES} from "angular2/router";

@Component({
    template: `
    
    <div class="wc-com-search-topthings">
        <h2 class="wc-com-search-minilogo">
            Search
        </h2>
        
        <wc-search-bar
            class="wc-com-search-bar"
            (searchTermsChange)="_searchTermsChange($event)"></wc-search-bar>
    </div>
    
    <wc-search-results [searchTerms]="_searchTerms"></wc-search-results>
    
    `,
    directives: [SearchBarCmp, ResultsCmp, ROUTER_DIRECTIVES],
    providers: [
        provide(FactoryService, {useClass: FactoryService})
    ]
})
export class SearchCmp {

    private _searchTerms = [];
    
    constructor(private _router: Router,
                private _routeParams: RouteParams,
                private _changeDetectionRef : ChangeDetectorRef){

    }

    // Private
    private _searchTermsChange(stc) {
        this._searchTerms = stc;
    }
}