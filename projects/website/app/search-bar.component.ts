import {Component} from 'angular2/core'
import {CompletionService} from './completion.service'
import {Observable} from "rxjs/Observable";
import {Completion} from "./completion"

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

`
})
export class SearchBarCmp {
    search = {
        text: '',
        uris: []
    }
    completion: Completion
    timeout = null

    constructor(private _completionService: CompletionService) {}

    keydown(event, enter) {
        event.preventDefault()
        if(enter && this.search.text.length === 0) {
            this.goSearching()
        } else if(this.search.text.length > 0) {
            if(this.search.uris.indexOf(this.search.text) === -1) {
                this.search.uris.push(this.search.text)
            }
            this.search.text = ''
            this.clearObservable()
            this.clearTimeout()
        }
    }

    change(event) {
        this.clearTimeout()
        this.timeout = setTimeout(function(self) {
            self.autoComplete()
        }, 500, this);
    }

    clearTimeout() {
        if(this.timeout) {
            clearTimeout(this.timeout)
        }
    }

    clearObservable() {
        this.completion = new Completion([])
    }

    autoComplete() {
        console.log('start')
        let current = this.completion
        let obs = this._completionService.list()
        let obs2: Observable<{obj: Completion, newValue: Completion}> = obs.map(com => {
            return {
                'obj': current,
                'newValue': com
            }
        })
        obs2.subscribe(tuple => {
            if(tuple.obj) {
                tuple.obj.update(tuple.newValue)
            } else {
                this.completion = tuple.newValue
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