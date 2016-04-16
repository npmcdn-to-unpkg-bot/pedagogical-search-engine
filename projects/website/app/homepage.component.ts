import {Component} from "angular2/core";
import {SearchCmp} from "./search/search.component";

@Component({
    selector: 'wc-homepage',
    template: `
    
    <h2>Home page</h2>
    <wc-search></wc-search>
    
    `,
    directives: [SearchCmp]
})
export class HomepageCmp {

}