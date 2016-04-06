import {Component, provide, Output, EventEmitter, Inject, Input, SimpleChange} from "angular2/core";
import {CompletionService} from "./completion.service";
import {MockCompletionService} from "./mock-completion.service";
import {Completion} from "./completion";
import {Resource} from "./resource";

@Component({
    selector: 'wc-completion',
    template: `

<h2>Completion of {{ _text }}, cursor {{ _cursor }}</h2>

<div class="wc-sb-c-entry"
    *ngFor="#proposition of _completion.getPropositions(); #i = index"
    [class.wc-sb-c-selected]="proposition.isSelected()"
    (click)="select()"
    (mouseover)="_setAndApplyCursor(i)">
    <span [textContent]="proposition | json"></span>
</div>
    
    `,
    providers: [
        provide(CompletionService, {useClass: MockCompletionService})
    ]
})
export class CompletionCmp {

    @Input("text") private _text = ''
    @Output("emptySelect") private _esEmitter = new EventEmitter();
    @Output("itemSelected") private _isEmitter = new EventEmitter<Resource>();

    private _timeout: number;
    private _completion: Completion = new Completion();
    private _latency: number = 500;
    private _cursor: number = 0;

    // Constructor
    constructor(
        @Inject(CompletionService) private _completionService: CompletionService) {
    }

    // Custom angular
    public ngOnChanges(changes: {_text: SimpleChange}) {
        if(!changes._text.isFirstChange()) {
            let newText = changes._text.currentValue;

            if(newText.length > 0) {
                this._clearTimeout();
                this._setTimeout();
            }
        }
    }

    // Public methods
    public select(event = null) {
        let clearThings: boolean = true;
        if(this._completion.hasPropositions()) {
            this._isEmitter.emit(this._completion.getProposition(this._cursor).getResource());
        } else {
            if(this._text.length == 0) {
                this._esEmitter.emit(event);
            } else {
                clearThings = false; // Let the propositions come
            }
        }

        if(clearThings) {
            // Reset everything
            this._clearTimeout();
            this._newCompletion();
            this._resetCursor();
        }
    }
    public down() {
        this._setAndApplyCursor(this._reframCursor(this._cursor + 1));
    }
    public up() {
        this._setAndApplyCursor(this._reframCursor(this._cursor - 1));
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
            this._setAndApplyCursor(0);
        })
    }
    private _newCompletion(completion: Completion = new Completion()) {
        this._completion.clear();
        delete this._completion; // help GC
        this._completion = completion;
    }
    private _isSelected(index: number): boolean {
        return this._completion.getProposition(index).isSelected();
    }
    private _resetCursor(): void {
        this._cursor = 0;
    }
    private _applyCursor(): void {
        this._completion.select(this._cursor);
    }
    private _reframCursor(pos: number): number {
        if(pos < 0) {
            return 0; // Left limit
        }

        let np = this._completion.nbOfPropositions();
        if(np == 0) {
            return 0; // Special case
        } else if(pos >= np) {
            return (np - 1);
        } else {
            return pos; // Right limit
        }
    }
    private _setAndApplyCursor(pos: number): void {
        this._cursor = pos;
        this._applyCursor();
    }
}
