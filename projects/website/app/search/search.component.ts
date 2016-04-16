import {Component, provide} from "angular2/core";
import {SearchBarCmp} from "./bar/bar.component";
import {FactoryService} from "./search-terms/factory.service";
import {ResultsCmp} from "./results/results.component";

@Component({
    selector: 'wc-search',
    template: `
    
    <h1>Search:</h1>
    
    <wc-search-bar></wc-search-bar>
    
    <wc-search-results></wc-search-results>
    
    `,
    directives: [SearchBarCmp, ResultsCmp],
    providers: [
        provide(FactoryService, {useClass: FactoryService})
    ]
})
export class SearchCmp {

}