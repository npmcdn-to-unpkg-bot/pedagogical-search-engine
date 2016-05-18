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
            Your searches and clicks is your data<br>
            but it is also a great fuel to improve the results<br>
            through statistical studies <b>if collected anonymously</b>.
        </p>
        
        <h3>What data do I collect</h3>
        <p>
            Wikichimp does not use cookies.<br>
            Instead, a <b>random identifier</b> is assigned to you and is saved on the local storage of your browser.<br><br>
            
            Your searches and on which results you click along with this identifier <br>
            are sent to my server and stored in my database.
        </p>
        <p class="wc-com-about-example">
            <b>Concretely</b> I can see in my database that user X has searched for terms Y and Z<br>
            and clicked on result A and B. And that later the same user has searched for other terms<br>
            and clicked on some other results.<br>
            <b>User X appears in the data anonymously behind a random number.</b><br>
            It is not possible to associate back this number with you personally.
        </p>
        
        <h3>How this data is used</h3>
        <p>
            I use this data to produce anonymous statistics for my master thesis.<br><br>
            
            <b>Concrete examples</b><br>
            <span class="wc-com-about-example">
                - How many results are clicked after each search in average?<br>
                - Do user click more on results of one kind or another?<br>
                - Do users search more topics of one kind or another?
            </span>
        </p>
    </div>
 
         `,
    directives: [ROUTER_DIRECTIVES],
    providers: []
})
export class AboutCmp {
}