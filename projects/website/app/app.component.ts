import {Component} from "angular2/core";
import {SearchBarCmp} from "./search-bar/search-bar.component";

@Component({
    selector: 'wc-app',
    template: `
    <h1>WikiChimp</h1>
    <wc-search-bar></wc-search-bar>
`,
    directives: [SearchBarCmp]
})
export class AppComponent {}

