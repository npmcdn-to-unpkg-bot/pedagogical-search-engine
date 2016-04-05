import {Component} from 'angular2/core'
import {CompletionService} from './completion.service'

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
            (keydown.tab)="userPush($event)"
            (keydown.enter)="userPush($event, true)"
            (ngModelChange)="change($event)">
        </div>
    </div>
    <div class="wc-sb-div3">
        <button [disabled]="search.uris.length < 1"
        (click)="goSearching()">Search</button>
    </div>
    <div class="wc-sb-c-div1">
        <div class="wc-sb-c-entry" *ngFor="#proposition of completion">
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
    completion = []
    timeout = null

    constructor(private _completionService: CompletionService) {}

    userPush(event, enter) {
        event.preventDefault()
        if(enter && this.search.text.length === 0) {
            this.goSearching()
        } else if(this.search.text.length > 0) {
            if(this.search.uris.indexOf(this.search.text) === -1) {
                this.search.uris.push(this.search.text)
            }
            this.search.text = ''
            this.completion = []
        }
    }

    change(event) {
        if(this.timeout !== null) {
            clearTimeout(this.timeout)
        }
        this.timeout = setTimeout(function(self) {
            self.autoComplete()
        }, 750, this);
    }

    autoComplete() {
        this._completionService.list().subscribe(
            value => {
                this.completion = value
            },
            error => console.log('cannot retrieve completions')
        )
    }

    remove(i) {
        this.search.uris.splice(i, 1);
    }

    goSearching() {
        console.log('go Searching!')
    }
}