import {Component, ViewChild} from "angular2/core";
import {CompletionCmp} from "./completion.component";
import {Resource} from "./resource";


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
                (keydown.tab)="_specialKeydown($event, 'tab')"
                (keydown.enter)="_specialKeydown($event, 'enter')"
                (ngModelChange)="_completionObj.change(text)">
        </div>
    </div>
    <div class="wc-sb-div3">
        <button [disabled]="_resources.length < 1"
        (click)="goSearching()">Search</button>
    </div>
    <wc-completion
        #completionObj
        (emptyEnter)="_emptyEnter()"
        (itemSelected)="_itemSelected($event)">
    </wc-completion>
</div>

`,
    directives: [CompletionCmp]
})
export class SearchBarCmp {
    private _text: String = '';
    private _resources: Array<Resource> = [];

    @ViewChild('completionObj') private _completionObj;

    // Public
    public goSearching() {
        console.log('go Searching!');
    }

    // Private
    private _emptyEnter() {
        this.goSearching();
    }
    private _itemSelected(item: Resource) {
        if(this._resources.indexOf(item) === -1) {
            this._resources.push(item);
        }
        this._text = '';
    }
    private _specialKeydown(event, type) {
        event.preventDefault();

        if(this._completionObj) {
            if(type === 'tab') {
                this._completionObj.tabDown();
            }
            if(type === 'enter') {
                this._completionObj.enterDown();
            }
        }
    }
    private _remove(i) {
        this._resources.splice(i, 1);
    }
}