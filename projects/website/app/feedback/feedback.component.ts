import {ROUTER_DIRECTIVES} from "angular2/router";
import {Component, ViewChild} from "angular2/core";
import {UserstudyService} from "../userstudy/userstudy";
import {MessageService} from "../message.service";
import {Q1Cmp} from "./questions/q1.component";
import {Q2Cmp} from "./questions/q2.component";
import {Q3Cmp} from "./questions/q3.component";
import {FeedbackService} from "./feedback.service";

@Component({
    selector: 'wc-app',
    template: `
    
    <div class="wc-com-feedback-container">

        <h2>A few questions</h2>
        
        <wc-feedback-q1></wc-feedback-q1>
        
        <wc-feedback-q2></wc-feedback-q2>
        
        <wc-feedback-q3></wc-feedback-q3>
        
        
        <p class="wc-com-feedback-question-statement">
             You can submit other feedback/suggestions
        </p>
        
        <form>
            <textarea [(ngModel)]="comment" class="wc-com-feedback-textarea"
                      maxlength="5000"></textarea>
            
            <div class="wc-com-feedback-button-container">
                <button (click)="_submitComment()">
                    Submit this comment
                </button>
                <span class="wc-com-feedback-saved-text"
                      [textContent]="_commentMsg"></span>
            </div>
        </form>
        
    </div>
 
         `,
    directives: [ROUTER_DIRECTIVES, Q1Cmp, Q2Cmp, Q3Cmp],
    providers: [
    ]
})
export class FeedbackCmp {
    constructor(private _msService: MessageService,
                private _feedbackService: FeedbackService) {
        if(this._feedbackService.hasBeenAnswered(this._id)) {
            this.comment = this._feedbackService.getAnswer(this._id);
            this._commentMsg = this._savedMsg;
        }
    }

    public comment;
    private _id = 'textfeedback';
    private _commentMsg = "";
    private _savedMsg = "You comment has been saved";

    private _submitComment(): void {
        this._commentMsg = "";
        this._feedbackService.saveAnswer(this._id, this.comment).subscribe(res => {
            this._commentMsg = this._savedMsg;
            console.log(res.statusText);
        });
    }
}