import {Component, Inject} from "angular2/core";
import {FeedbackService} from "../feedback.service";

@Component({
    selector: 'wc-feedback-q2',
    template: `

        <p class="wc-com-feedback-question-statement">
             Does the website give you any results for your searches?
        </p>
        
        <form>
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_click('no')" type="radio" name="question"> No in most cases
                <span [textContent]="_getMsg('no')"
                      class="wc-com-feedback-saved-text"></span>
            </label>
            
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_click('yes')" type="radio" name="question"> Yes in most cases
                <span [textContent]="_getMsg('yes')"
                      class="wc-com-feedback-saved-text"></span>
            </label>
            
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_click('joker')" type="radio" name="question"> Joker: I do not answer
                <span [textContent]="_getMsg('joker')"
                      class="wc-com-feedback-saved-text"></span>
            </label>
        </form>
    
    `
})
export class Q2Cmp {
    constructor(@Inject(FeedbackService) private _feedbackService: FeedbackService) {}
    
    private _id = 'Q2';
    
    private _click(value: string): void {
        this._feedbackService.saveAnswer(this._id, value).subscribe(res => console.log(res.text()));
    }

    private _getMsg(value: string): string {
        if(this._feedbackService.hasBeenAnswered(this._id) &&
            this._feedbackService.getAnswer(this._id) === value) {
            return "Your answer has been saved";
        } else {
            return "";
        }
    }
}