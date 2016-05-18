import {Component, Inject} from "angular2/core";
import {FeedbackService} from "../feedback.service";

@Component({
    selector: 'wc-feedback-q3',
    template: `

        <p class="wc-com-feedback-question-statement">
             When the website gives you results, are they good?
        </p>
        
        <form>
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_click('worse')"
                       [checked]="wasAnswered('worse')"
                       type="radio" name="question">
                No, it is often worse than Google or Bing
                <span [textContent]="_getMsg('worse')"
                      class="wc-com-feedback-saved-text"></span>
            </label>
            
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_click('equivalent')" 
                       [checked]="wasAnswered('equivalent')"
                       type="radio" name="question">
                It is equivalent
                <span [textContent]="_getMsg('equivalent')"
                      class="wc-com-feedback-saved-text"></span>
            </label>
            
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_click('potential')" 
                       [checked]="wasAnswered('potential')"
                       type="radio" name="question">
                It is sometimes better, but not worse
                <span [textContent]="_getMsg('potential')"
                      class="wc-com-feedback-saved-text"></span>
            </label>
            
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_click('better')" 
                       [checked]="wasAnswered('better')"
                       type="radio" name="question">
                It is usually better
                <span [textContent]="_getMsg('better')"
                      class="wc-com-feedback-saved-text"></span>
            </label>
            
            <label class="wc-com-feedback-radiochoice">
                <input (click)="_click('joker')" 
                       [checked]="wasAnswered('joker')"
                       type="radio" name="question"> Joker: I do not answer
                <span [textContent]="_getMsg('joker')"
                      class="wc-com-feedback-saved-text"></span>
            </label>
        </form>
    
    `
})
export class Q3Cmp {
    constructor(@Inject(FeedbackService) private _feedbackService: FeedbackService) {}
    
    private _id = 'Q3';
    
    private _click(value: string): void {
        this._feedbackService.saveAnswer(this._id, value).subscribe(res => console.log(res.text()));
    }

    private _getMsg(value: string): string {
        if(this.wasAnswered(value)) {
            return "Your answer has been saved";
        } else {
            return "";
        }
    }

    private wasAnswered(value: string)
    : boolean {
        return (this._feedbackService.hasBeenAnswered(this._id) &&
        this._feedbackService.getAnswer(this._id) === value);
    }
}