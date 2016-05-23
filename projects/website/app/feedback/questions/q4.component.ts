import {Component, Inject, Input} from "angular2/core";
import {FeedbackService} from "../feedback.service";

@Component({
    selector: 'wc-feedback-q4',
    template: `

        <div *ngIf="!_inline">
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
        </div>
        
        <div *ngIf="_inline">
            <span class="wc-com-feedback-question-inlinestatement">
                <span [textContent]="_text"></span>
                &#187;
            </span>
            
            <form class="wc-com-feedback-inlineform">
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
        </div>
    
    `
})
export class Q4Cmp {
    constructor(@Inject(FeedbackService) private _feedbackService: FeedbackService) {}

    @Input("text") private _text = '';
    @Input("id") private _id = 'Q4';
    @Input("inline") private _inline: boolean = false;
    @Input("supplement") private _supplement = null;
    @Input("supplementHash") private _supplementHash: string = null;
    
    private _click(v: string): void {
        let value;
        if(this._supplement != null) {
            value = this._supplement;
            value.score = v;
            this._feedbackService.saveAnswer2(this._id, this._supplementHash, value).subscribe(res =>
                console.log(res.statusText)
            );
        } else {
            this._feedbackService.saveAnswer(this._id, v).subscribe(res =>
                console.log(res.statusText)
            );
        }
    }

    private _getMsg(): string {
        let msg = "saved";
        let emptyMsg = "";
        if(this._hasSupplement()) {
            if(this._feedbackService.hasBeenAnswered2(this._id, this._getId2())) {
                return msg;
            } else {
                return emptyMsg;
            }
        } else {
            if(this._feedbackService.hasBeenAnswered(this._id)) {
                return msg;
            } else {
                return emptyMsg;
            }
        }
    }

    private _hasSupplement()
    : boolean {
        return (this._supplement != null);
    }

    private _getId2()
    : string {
        return this._supplementHash;
    }

    private wasAnswered(value: string)
    : boolean {
        if(this._hasSupplement()) {
            return (this._feedbackService.hasBeenAnswered2(this._id, this._getId2()) &&
            this._feedbackService.getAnswer2(this._id, this._getId2()).score === value);
        } else {
            return (this._feedbackService.hasBeenAnswered(this._id) &&
            this._feedbackService.getAnswer(this._id) === value);
        }
    }
}