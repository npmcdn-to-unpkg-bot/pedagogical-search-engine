import {Component, provide, Inject} from "angular2/core";
import {CompletionService} from "./completion.service";
import {Completion} from "./completion";
import {MockCompletionService} from "./mock-completion.service";


@Component({
    selector: 'wc-search-bar',
    template: `

<div class="wc-sb-div1">
    <div class="wc-sb-div2">
        <div class="wc-sb-div2l" *ngFor="#uri of search.uris; #i = index">
            <span [textContent]="uri"></span>
            <span (click)="remove(i)">&#x2715;</span>
        </div>
        <div class="wc-sb-div2r">
            <input type="text" [(ngModel)]="search.text"
            (keydown.tab)="keydown($event)"
            (keydown.enter)="keydown($event, true)"
            (ngModelChange)="change($event)">
        </div>
    </div>
    <div class="wc-sb-div3">
        <button [disabled]="search.uris.length < 1"
        (click)="goSearching()">Search</button>
    </div>
    <div class="wc-sb-c-div1">
        <div class="wc-sb-c-entry" *ngFor="#proposition of completion?.propositions">
            <span [textContent]="proposition.label"></span>
        </div>
    </div>
</div>
<p>
    {{ completion | json }}
</p>

`,
    providers: [
        provide(CompletionService, {
            useClass: MockCompletionService
        })
    ]
})
export class SearchBarCmp {
    public search = {
        text: '',
        uris: []
    }
    public completion:Completion
    public timeout = null

    constructor(@Inject(CompletionService) private _completionService:CompletionService) {
    }

    keydown(event, enter) {
        event.preventDefault()
        if (enter && this.search.text.length === 0) {
            this.goSearching()
        } else if (this.search.text.length > 0) {
            if (this.search.uris.indexOf(this.search.text) === -1) {
                this.search.uris.push(this.search.text)
            }
            this.search.text = ''
            this.clearObservable()
            this.clearTimeout()
        }
    }

    change(event) {
        this.clearTimeout()
        this.timeout = setTimeout(function (self) {
            self.autoComplete()
        }, 500, this);
    }

    clearTimeout() {
        if (this.timeout) {
            clearTimeout(this.timeout)
        }
    }

    clearObservable() {
        delete this.completion
        this.completion = new Completion([])
    }

    autoComplete() {
        console.log('start')
        let current = this.completion

        this._completionService.list().map(com => {
            return {'lastObjRef': current, 'newObjRef': com}
        }).subscribe(t => {
            if (t.lastObjRef) {
                t.lastObjRef.update(t.newObjRef)
                delete t.newObjRef
            } else {
                this.completion = t.newObjRef
            }
        })
    }

    remove(i) {
        this.search.uris.splice(i, 1);
    }

    goSearching() {
        console.log('go Searching!')
    }
}