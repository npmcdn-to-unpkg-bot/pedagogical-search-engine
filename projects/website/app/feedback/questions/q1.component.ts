import {Component, Inject} from "angular2/core";
import {FeedbackService} from "../feedback.service";

@Component({
    selector: 'wc-feedback-q1',
    template: `

        <p class="wc-com-feedback-question-statement">
             Do you find the interface intuitive enough?
        </p>
        
        <form>
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_clickQ1('no')" type="radio" name="Q1"> No
                <span [textContent]="_getMsg('no')"
                      class="wc-com-feedback-saved-text"></span>
            </label>
            
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_clickQ1('yes')" type="radio" name="Q1"> Yes
                <span [textContent]="_getMsg('yes')"
                      class="wc-com-feedback-saved-text"></span>
            </label>
            
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_clickQ1('joker')" type="radio" name="Q1"> Joker: I do not answer
                <span [textContent]="_getMsg('joker')"
                      class="wc-com-feedback-saved-text"></span>
            </label>
        </form>
    
    `
})
export class Q1Cmp {
    constructor(@Inject(FeedbackService) private _feedbackService: FeedbackService) {}
    
    private _id = 'Q1';
    
    private _clickQ1(value: string): void {
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