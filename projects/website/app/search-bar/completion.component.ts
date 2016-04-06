import {Component, provide, Output, EventEmitter, Inject} from "angular2/core";
import {CompletionService} from "./completion.service";
import {MockCompletionService} from "./mock-completion.service";
import {Completion} from "./completion";
import {Resource} from "./resource";

@Component({
    selector: 'wc-completion',
    template: `

<h2>Completion</h2>

<div class="wc-sb-c-div1">
    <div class="wc-sb-c-entry" *ngFor="#proposition of _completion.getPropositions()">
        <span [textContent]="proposition | json"></span>
    </div>
</div>
    
    `,
    providers: [
        provide(CompletionService, {useClass: MockCompletionService})
    ]
})
export class CompletionCmp {

    @Output("emptyEnter") eeEmitter = new EventEmitter();
    @Output("itemSelected") isEmitter = new EventEmitter<Resource>();

    private _timeout: number;
    private _completion: Completion = new Completion();
    private _latency: number = 500;

    // Constructor
    constructor(
        @Inject(CompletionService) private _completionService: CompletionService) {
    }

    // Public methods
    public enterDown() {
        if(this._completion.hasPropositions()) {
            this.isEmitter.emit(this._completion.getPropositions()[0]);
        } else {
            this.eeEmitter.emit("");
        }

        // Reset everything
        this._clearTimeout();
        this._newCompletion();
    }
    public tabDown() {
        console.log("tab (unhandled)");
    }
    public change(text: String) {
        this._clearTimeout();
        this._setTimeout();
    }

    // Private
    private _clearTimeout() {
        if (this._timeout) {
            clearTimeout(this._timeout)
        }
    }
    private _setTimeout() {
        this._timeout = setTimeout(function (self) {
            self._autoComplete()
        }, this._latency, this);
    }
    private _autoComplete() {
        console.log('start');
        let currentRef = this._completion;

        this._completionService.list().map(newRef => {
            return {'currentRef': currentRef, 'newRef': newRef}
        }).subscribe(t => {
            t.currentRef.update(t.newRef.getPropositions());
        })
    }
    private _newCompletion(completion: Completion = new Completion()) {
        this._completion.clear();
        delete this._completion; // help GC
        this._completion = completion;
    }
}
