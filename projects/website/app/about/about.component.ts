import {ROUTER_DIRECTIVES} from "angular2/router";
import {Component} from "angular2/core";
import {MessageService} from "../message.service";
import {UserstudyService} from "../userstudy/userstudy";

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
        
        <h2>What data do I collect</h2>
        <p>
            I do log <b>anonymously</b> some of your activity: your clicks and searches.<br>
        </p>
        
        <p>
            A <b>random identifier</b> is assigned to you<br>
            and is saved on the local storage of your browser.<br><br>
            
            Your searches and on which results you click along with this identifier <br>
            are sent to the server and stored in the database.
        </p>
        
        <h3>How this data is used</h3>
        <p>
            I use this data to produce anonymous statistics for my master thesis.<br><br>
            
            <b>Concrete examples</b><br>
            <span class="wc-com-about-example">
                - How many results are clicked after each search in average?<br>
                - Do user click more on results of one kind than another?<br>
                - Do users search more topics of one kind than another?
            </span>
        </p>
        
        <h3>Change your preferences</h3>
        <p>
            Your current logging status:
            <b>
                <span class="wc-com-colors-codes-bad"
                      *ngIf="_isDisabled()">Disabled</span>
                <span class="wc-com-colors-codes-good"
                      *ngIf="!_isDisabled()">Enabled</span>
            </b>
            
            <button (click)="_disable()" 
                    *ngIf="!_isDisabled()"
                    [disabled]="_disableButton">
                Switch Off
            </button>
            <button (click)="_enable()"
                    *ngIf="_isDisabled()"
                    [disabled]="_disableButton">
                Switch On
            </button>
        </p>
    </div>
 
         `,
    directives: [ROUTER_DIRECTIVES],
    providers: []
})
export class AboutCmp {
    constructor(private _mService: MessageService,
                private _usService: UserstudyService) {}

    private _disableButton: boolean = false;

    private _isDisabled()
    : boolean {
        return this._usService.isDisabled();
    }
    
    private _disable()
    : void {
        let stream = this._mService.log('forget-user', this._usService.sid + "");
        this._disableButton = true;
        stream.subscribe(res => {
            this._disableButton = false;
            this._usService.disable();
        });
    }

    private _enable()
    : void {
        this._usService.enable();
        this._disableButton = true;
        let stream = this._mService.log('unforget-user', this._usService.sid + "");
        stream.subscribe(res => {
            this._disableButton = false;
        });
    }
}