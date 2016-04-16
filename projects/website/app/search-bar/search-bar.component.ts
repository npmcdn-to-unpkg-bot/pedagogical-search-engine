import {Component, ViewChild} from "angular2/core";
import {CompletionCmp} from "./completion/completion.component";
import {Result} from "./completion/result/result";

enum keys {Tab, Enter, Down, Up, Escape};

@Component({
    selector: 'wc-search-bar',
    template: `

<div class="wc-sb-div1">
    <div class="wc-sb-div2">
        <div class="wc-sb-div2l" *ngFor="#resource of _resources; #i = index">
            <span [textContent]="resource | json"></span>
            <span (click)="_remove(i)">&#x2715;</span>
        </div>
        <div class="wc-sb-div2r">
            <input type="text" [(ngModel)]="_text"
                (keydown.tab)="_specialKeydown($event, _KEYS.Tab)"
                (keydown.enter)="_specialKeydown($event, _KEYS.Enter)"
                (keydown.ArrowDown)="_specialKeydown($event, _KEYS.Down)"
                (keydown.ArrowUp)="_specialKeydown($event, _KEYS.Up)"
                (keydown.Escape)="_specialKeydown($event, _KEYS.Escape)"
                #inputObj>
        </div>
    </div>
    <div class="wc-sb-div3">
        <button [disabled]="_resources.length < 1"
        (click)="goSearching()">Search</button>
    </div>
    <wc-completion
        #completionObj
        [text]="_text"
        (emptySelect)="_emptySelect($event)"
        (itemSelected)="_itemSelected($event)">
    </wc-completion>
</div>

`,
    directives: [CompletionCmp]
})
export class SearchBarCmp {
    private _text: String = '';
    private _resources: Array<Result> = [];

    private _KEYS = keys;

    @ViewChild('completionObj') private _completionObj;
    @ViewChild('inputObj') private _input;

    // Init logic
    constructor(private _window: Window) {}

    public ngAfterViewInit(): void {
        let input = this._input;
        this._window.addEventListener("keydown", function(event){
            input.nativeElement.focus();
        });
    }

    // Public
    public goSearching() {
        console.log('go Searching!');
    }

    // Private
    private _emptySelect(event) {
        if(event === keys.Enter) {
            this.goSearching();
        }
    }
    private _itemSelected(item: Result) {
        if(this._resources.indexOf(item) === -1) {
            this._resources.push(item);
        }
        this._text = '';
    }
    private _specialKeydown(event, type) {
        event.preventDefault();

        if(this._completionObj) {
            if(type === keys.Tab) {
                this._completionObj.select(keys.Tab);
            }
            if(type === keys.Enter) {
                this._completionObj.select(keys.Enter);
            }
            if(type === keys.Down) {
                this._completionObj.down();
            }
            if(type === keys.Up) {
                this._completionObj.up();
            }
            if(type === keys.Escape) {
                this._completionObj.escape();
            }
        }
    }
    private _remove(i) {
        this._resources.splice(i, 1);
    }
}