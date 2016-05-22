import {Component, Inject, Input} from "angular2/core";
import {FeedbackService} from "../feedback.service";

@Component({
    selector: 'wc-feedback-q4',
    template: `

        <p class="wc-com-feedback-question-statement"
           [textContent]="_text"></p>
        
        <form>
            <label class="wc-com-feedback-inlineradiochoice">
                <input (click)="_click('1')"
                       [checked]="wasAnswered('1')"
                       type="radio" name="question">
                not useful
            </label>
            
            <label class="wc-com-feedback-inlineradiochoice">
                <input (click)="_click('2')"
                       [checked]="wasAnswered('2')"
                       type="radio" name="question">
                little useful
            </label>
            
            <label class="wc-com-feedback-inlineradiochoice">
                <input (click)="_click('3')"
                       [checked]="wasAnswered('3')"
                       type="radio" name="question">
                useful
            </label>
            
            <label class="wc-com-feedback-inlineradiochoice">
                <input (click)="_click('4')"
                       [checked]="wasAnswered('4')"
                       type="radio" name="question">
                very useful
            </label>
            
            <span [textContent]="_getMsg()"
                  class="wc-com-feedback-saved-text"></span>
        </form>
    
    `
})
export class Q4Cmp {
    constructor(@Inject(FeedbackService) private _feedbackService: FeedbackService) {}

    @Input("text") private _text = '';
    @Input("id") private _id = 'Q4';
    
    private _click(value: string): void {
        this._feedbackService.saveAnswer(this._id, value).subscribe(res => console.log(res.text()));
    }

    private _getMsg(): string {
        if(this._feedbackService.hasBeenAnswered(this._id)) {
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