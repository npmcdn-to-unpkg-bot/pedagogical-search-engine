import {Component, provide} from "angular2/core";
import {SearchBarCmp} from "./bar/bar.component";
import {ResultsCmp} from "./results/results.component";
import {FactoryService} from "./search-terms/factory.service";
import {RouteParams} from "angular2/router";

@Component({
    template: `
    
    <h2>Search page</h2>
    <wc-search-bar></wc-search-bar>
    <wc-search-results></wc-search-results>
    
    `,
    directives: [SearchBarCmp, ResultsCmp],
    providers: [
        provide(FactoryService, {useClass: FactoryService})
    ]
})
export class SearchPageCmp {
    constructor(private _routeParams: RouteParams){}

    ngOnInit() {
        let searchParameters = JSON.parse(decodeURI(this._routeParams.get('q')));
        console.log(searchParameters);
    }
}