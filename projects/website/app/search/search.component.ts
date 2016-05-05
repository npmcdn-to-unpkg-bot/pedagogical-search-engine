import {Component, provide, ChangeDetectorRef} from "angular2/core";
import {SearchBarCmp} from "./bar/bar.component";
import {ResultsCmp} from "./results/results.component";
import {FactoryService} from "./search-terms/factory.service";
import {RouteParams, Router, ROUTER_DIRECTIVES} from "angular2/router";

@Component({
    template: `
    
    <div class="wc-com-search-topthings">
        <wc-search-bar
            class="wc-com-search-bar-container"
            (searchTermsChange)="_searchTermsChange($event)"></wc-search-bar>
    </div>
    
    <wc-search-results [searchTerms]="_searchTerms"></wc-search-results>
    
    <div *ngIf="_displayVideo" class="wc-com-search-video">
        <iframe 
            width="448"
            height="252"
            src="https://www.youtube-nocookie.com/embed/V66WSq9A36k?rel=0&amp;controls=0&amp;showinfo=0"
            frameborder="0"
            allowfullscreen></iframe>
    </div>
    
    
    `,
    directives: [SearchBarCmp, ResultsCmp, ROUTER_DIRECTIVES],
    providers: [
        provide(FactoryService, {useClass: FactoryService})
    ]
})
export class SearchCmp {

    private _searchTerms = [];
    private _displayVideo: boolean = true;
    
    constructor(private _router: Router,
                private _routeParams: RouteParams,
                private _changeDetectionRef : ChangeDetectorRef){

    }

    // Private
    private _searchTermsChange(stc) {
        this._searchTerms = stc;
        if(stc && stc.length > 0) {
            this._displayVideo = false;
        }
    }
}