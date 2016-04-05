import {Component} from 'angular2/core'
import {CompletionService} from './completion.service'

@Component({
    selector: 'wc-search-bar',
    template: `

<div class="wc-sb-div1">
    <div class="wc-sb-div2">
        <div class="wc-sb-div2l" *ngFor="#uri of search.uris" [textContent]="uri">
            {{ uri }}
        </div>
        <div class="wc-sb-div2r">
            <input type="text" [(ngModel)]="search.text"
            (keydown.tab)="userPush($event)"
            (keydown.enter)="userPush($event, true)">
        </div>
    </div>
    <div class="wc-sb-div3">
        <button [disabled]="search.uris.length < 1">Search</button>
    </div>
</div>
<p>
    {{ search | json }}
</p>

`
})
export class SearchBarCmp {
    search = {
        text: '',
        uris: []
    }

    constructor(private _completionService: CompletionService) {}

    userPush(event, enter) {
        event.preventDefault()
        if(enter && this.search.text.length === 0) {
            this.goSearching()
        } else if(this.search.text.length > 0) {
            this.search.uris.push(this.search.text)
            this.search.text = ''
        }
    }

    goSearching() {
        console.log('go Searching!')
    }

    list() {
        return this._completionService.list()
    }
}