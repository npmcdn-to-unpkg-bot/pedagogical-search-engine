import {Component, provide, ChangeDetectorRef} from "angular2/core";
import {SearchBarCmp} from "./bar/bar.component";
import {ResultsCmp} from "./results/results.component";
import {FactoryService} from "./search-terms/factory.service";
import {RouteParams, Router, ROUTER_DIRECTIVES} from "angular2/router";
import {HelperService} from "../helper/helper.service";

@Component({
    template: `
        
    <div class="wc-com-search-topthings">
        <wc-search-bar
            class="wc-com-search-bar-container"
            (searchTermsChange)="_searchTermsChange($event)"></wc-search-bar>
    </div>
    
    <wc-search-results [searchTerms]="_searchTerms"></wc-search-results>
    
    <div *ngIf="_displayVideo" class="wc-com-search-video">
    
        <span *ngIf="!_helper.hasBeenDisplayed('1')">
            <span class="wc-com-helper-msg">
                (1) Type in your query &#8599;<br>
                ex: Biology 
            </span>
        </span>
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
                private _changeDetectionRef : ChangeDetectorRef,
                private _helper: HelperService){

    }

    // Private
    private _searchTermsChange(stc) {
        this._searchTerms = stc;
        if(stc && stc.length > 0) {
            this._helper.setAsDisplayed('1');
            if(stc.length > 0 && this._helper.hasBeenDisplayed('2')) {
                this._helper.setAsDisplayed('3');
            }
            if(stc.length > 1) {
                this._helper.setAsDisplayed('2');
            }

            this._displayVideo = false;
        }
    }
}