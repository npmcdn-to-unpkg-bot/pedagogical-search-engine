import {ROUTER_DIRECTIVES} from "angular2/router";
import {Component} from "angular2/core";

@Component({
    selector: 'wc-app',
    template: `

    <div class="wc-com-about-container">
        <h2>What is Wikichimp</h2>
        <p>
             Wikichimp is a search engine.<br>
             It helps browsing pedagogical documents like books & online courses.
        </p>
        
        <p>
            It tries a novel approach and is my ongoing master thesis.
        </p>
        
        <!-- <h2>How to contribute</h2> Talk to me -->
        
        <h2>What about Privacy</h2>
        <p>
            Your searches and clicks is your data
        </p>
        
        <p>
            but it is also a great fuel to improve the <br>
            results through statistical studies if collected anonymously.
        </p>
        
         <h3>Here is my compromise</h3>
        <p>
            I forget about you as soon as you leave. <br>
            No cookies, no browser tracking.
        </p>
        
        <p>
            I log the clicks, the searches but
            I do not associate this data with you.
        </p>
    </div>
 
         `,
    directives: [ROUTER_DIRECTIVES],
    providers: []
})
export class AboutCmp {
}