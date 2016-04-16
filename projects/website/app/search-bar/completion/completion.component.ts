import {Component, provide, Output, EventEmitter, Inject, Input, SimpleChange} from "angular2/core";
import {CompletionService} from "./completion.service";
import {Completion} from "./completion";
import {SimpleCompletionService} from "./simple-completion.service";
import {Result} from "./result/result";
import {Disambiguation} from "./result/disambiguation";
import {Proposition} from "./proposition";

@Component({
    selector: 'wc-completion',
    template: `

<h2>Completion of {{ _text }}</h2>

<div class="wc-sb-c-entry"
    *ngFor="#proposition of getPropositions(); #i = index"
    [class.wc-sb-c-selected]="proposition.isSelected()"
    [class.wc-sb-c-disambiguation]="proposition.getResult().isDisambiguation()"
    (click)="select()"
    (mouseover)="_setAndApplyCursor(i)">
    <span [textContent]="proposition.getResult().label | json"></span>
</div>
    
    `,
    providers: [
        provide(CompletionService, {useClass: SimpleCompletionService})
    ]
})
export class CompletionCmp {

    @Input("text") private _text = '';
    @Output("emptySelect") private _esEmitter = new EventEmitter();
    @Output("itemSelected") private _isEmitter = new EventEmitter<Result>();

    private _timeout: number;
    private _completion: Completion = new Completion();
    private _disambiguationCompletion: Completion = new Completion();
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
    public getPropositions(): Array<Proposition> {
        if(this._disambiguationCompletion.hasPropositions()) {
            return this._disambiguationCompletion.getPropositions();
        } else {
            return this._completion.getPropositions();
        }
    }

    public getSelectedResult(): Result {
        if(this._disambiguationCompletion.hasPropositions()) {
            return this._disambiguationCompletion.getProposition(this._cursor).getResult();
        } else {
            return this._completion.getProposition(this._cursor).getResult();
        }
    }

    public select(event = null): void {
        let clearThings: boolean = true;
        if(this._completion.hasPropositions()) {
            let selected = this.getSelectedResult();
            if(selected.isDisambiguation()) {
                this._disambiguationCompletion = new Completion(selected.asDisambiguation().entities);
                this._setAndApplyCursor(0);
                clearThings = false;
            } else {
                this._isEmitter.emit(selected);
            }
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
            this._newCompletions();
            this._resetCursor();
        }
    }
    public down() {
        this._setAndApplyCursor(this._reframeCursorToClosest(this._cursor + 1));
    }
    public up() {
        this._setAndApplyCursor(this._reframeCursorToClosest(this._cursor - 1));
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

        this._completionService.list(this._text).map(newRef => {
            return {'currentRef': currentRef, 'newRef': newRef}
        }).subscribe(t => {
            t.currentRef.update(t.newRef.getPropositions());
            this._setAndApplyCursor(this._reframeCursorToClosest(this._cursor));
        })
    }
    private _newCompletions(
        completion: Completion = new Completion(),
        disambiguationCompletion: Completion = new Completion()) {
        // Clear the completions
        this._completion.clear();
        this._disambiguationCompletion.clear();

        // Help the GC
        delete this._completion;
        delete this._disambiguationCompletion;

        // Assign the new completions
        this._completion = completion;
        this._disambiguationCompletion = disambiguationCompletion;
    }
    private _isSelected(index: number): boolean {
        return this._completion.getProposition(index).isSelected();
    }
    private _resetCursor(): void {
        this._cursor = 0;
    }
    private _applyCursor(): void {
        if(this._disambiguationCompletion.hasPropositions() > 0) {
            this._disambiguationCompletion.select(this._cursor);
        } else {
            this._completion.select(this._cursor);
        }
    }
    private _reframeCursorToClosest(pos: number): number {
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
