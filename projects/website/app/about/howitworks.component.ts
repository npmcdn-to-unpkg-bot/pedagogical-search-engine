import {ROUTER_DIRECTIVES} from "angular2/router";
import {Component} from "angular2/core";

@Component({
    selector: 'wc-app',
    template: `
    
    <div class="wc-com-hiw-container">
        <h2>How it works</h2>
        
        <h3>1 - Typing a query</h3>
        <p>
            Wikichimp first helps the user to type in the query.<br><br>
            
            It searches the query on <a href="https://www.wikipedia.org/">wikipedia.org</a> and returns
            the best completions based on the popularity of the concept.<br><br>
            
            The first topics suggested are the ones with more and better results.
        </p>
        <p class="wc-com-hiw-example">
            Typing in "biology", Wikichimp will suggest "biology", "biology-chemistry", "biology cell", ...
        </p>
        
        <h3>2 - Retrieving the best documents</h3>
        <p>
            Wikichimp matches the query terms with a database of documents.<br><br>
            
            The texts associated with each document are scanned to search for the query in a
            <a href="https://en.wikipedia.org/wiki/Full_text_search">Full text search</a> manner.<br><br>
            
        </p>
        
        <h3>3 - More advanced search</h3>
        <p>
            A more advanced search is performed if the query contains concepts that match Wikipedia articles.<br><br>
            
            Wikichimp searches a more advanced database for those concepts.<br><br>
            
            Each document has many candidate concepts mentioned.
            Wikichimp analyses the correlation between them and filter out suspect concepts.<br><br>
        </p>
        
        <p class="wc-com-hiw-example">
            A document might talk about <a href="https://en.wikipedia.org/wiki/IPhone">iphones</a> and the 
            <a href="https://en.wikipedia.org/wiki/Apple_Inc.">Apple Company (Apple Inc.)</a>
            In this case, the concept <a href="https://en.wikipedia.org/wiki/Apple">"apple" (the fruit)</a>
            is a candidate which is discarded since it has no <b>strong</b> connections with iphones or the Apple Company.
        </p>
    </div>
 
         `,
    directives: [ROUTER_DIRECTIVES],
    providers: [
    ]
})
export class HowItWorksCmp {
    constructor() {}
}