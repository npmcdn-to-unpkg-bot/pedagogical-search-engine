import {ROUTER_DIRECTIVES} from "angular2/router";
import {Component} from "angular2/core";

@Component({
    selector: 'wc-app',
    template: `
    
    <div class="wc-com-feedback-container">
        <h2>A few questions</h2>
        <p class="wc-com-feedback-question-statement">
             Do you find the interface intuitive enough?
        </p>
        
        <form>
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_clickQ1('no')" type="radio" name="Q1"> No
            </label>
            
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_clickQ1('yes')" type="radio" name="Q1"> Yes
            </label>
            
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_clickQ1('joker')" type="radio" name="Q1"> Joker: I do not answer
            </label>
        </form>
        
        <p class="wc-com-feedback-question-statement">
             Does the website give you any results for your searches?
        </p>
        
        <form>
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_clickQ2('no')" type="radio" name="Q2"> No in most cases
            </label>
            
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_clickQ2('yes')" type="radio" name="Q2"> Yes in most cases
            </label>
            
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_clickQ2('joker')" type="radio" name="Q2"> Joker: I do not answer
            </label>
        </form>
        
        <p class="wc-com-feedback-question-statement">
             When the website gives you results, are they good?
        </p>
        
        <form>
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_clickQ3('worse')" type="radio" name="Q3">
                No, it is often worse than Google or Bing
            </label>
            
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_clickQ3('equivalent')" type="radio" name="Q3">
                It is equivalent
            </label>
            
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_clickQ3('potential')" type="radio" name="Q3">
                It is sometimes better, but not worse
            </label>
            
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_clickQ3('better')" type="radio" name="Q3">
                It is usually better
            </label>
            
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_clickQ3('joker')" type="radio" name="Q2"> Joker: I do not answer
            </label>
        </form>
        
    </div>
 
         `,
    directives: [ROUTER_DIRECTIVES],
    providers: [
    ]
})
export class FeedbackCmp {
    constructor() {}

    private _clickQ1(value: string): void {
        console.log(`Q1: Clicked ${value}`);
    }
    private _clickQ2(value: string): void {
        console.log(`Q1: Clicked ${value}`);
    }
    private _clickQ3(value: string): void {
        console.log(`Q1: Clicked ${value}`);
    }
}