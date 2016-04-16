import {Component} from "angular2/core";
import {RouteParams} from "angular2/router";

@Component({
    selector: 'wc-search-results',
    template: `
    
    <p>.. some results</p>
    
    `,
    directives: []
})
export class ResultsCmp {
    constructor(private _routeParams: RouteParams) {}
}
