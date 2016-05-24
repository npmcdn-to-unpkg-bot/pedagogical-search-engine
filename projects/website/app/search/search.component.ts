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
        <h3>The experiment [2 minutes]</h3>
        
        <ol>
            <li>Chose a topic you want to learn more about</li>
            <li>Type your topic in the search bar above</li>
            <li>Go through the results and give a rating to each one</li>
            <li>
                Answer 4 simple questions
                <a [routerLink]="['Feedback']">
                    on the feedback page
                </a>
            </li>
        </ol>
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