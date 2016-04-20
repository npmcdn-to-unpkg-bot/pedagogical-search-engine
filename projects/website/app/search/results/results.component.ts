import {Component, Input, provide, Inject, SimpleChange} from "angular2/core";
import {SearchTerm} from "../search-terms/SearchTerm";
import {EntriesService} from "./entries.service";
import {Entry} from "./entry";
import {SimpleEntriesService} from "./simple-entries.service";
import {Response} from "./response";

@Component({
    selector: 'wc-search-results',
    template: `
    
<div *ngFor="#entry of _response?.entries" style="padding: 5px;">
    <div>
        <a [href]="entry.href">
            <b [textContent]="entry.title"></b>
        </a>
        <i [textContent]="entry.typeText"></i>
        (<span [textContent]="entry.score"></span>)
    </div>
    <div *ngFor="#line of entry.snippet.lines">
        <div [textContent]="line.text"></div>
    </div>
</div>
<div *ngIf="_hasMultiplePages()">
    <div *ngFor="#pageNo of _pages()">
        <a [textContent]="pageNo"
        (click)="_goPage($event, pageNo)">a</a>
    </div>
</div>
    
    `,
    directives: [],
    providers: [
        provide(EntriesService, {useClass: SimpleEntriesService})
    ]
})
export class ResultsCmp {

    @Input('searchTerms') private _searchTerms: Array<SearchTerm> = [];

    private _response: Response;
    private _from: number = 0;
    private _step: number = 10;

    constructor(
        @Inject(EntriesService) private _entriesService: EntriesService
    ) {}

    // Life-cycle hooks
    ngOnChanges(changes: {_searchTerms: SimpleChange}) {
        let sts = changes._searchTerms.currentValue;
        if(sts && sts.length > 0) {
            this._fetchEntries();
        }
    }

    // Private
    private _fetchEntries(): void {
        let entriesObs = this._entriesService.list(
            this._searchTerms,
            this._from,
            this._to()
        );
        entriesObs.subscribe(res => {
            this._response = res;
        });
    }
    private _hasMultiplePages(): boolean {
        if(this._response) {
            return this._response.nbResults > this._step;
        } else {
            return false;
        }
    }
    private _to(): number {
        return this._from + this._step - 1;
    }
    private _nbPage(): number {
        if(!this._response) {
            return 0;
        } else {
            return Math.ceil(this._response.nbResults / this._step);
        }
    }
    private _pages(): Array<number> {
        let b = [];
        for(let i = 1; i <= this._nbPage(); i++) {
            b.push(i);
        }
        return b;
    }
    private _goPage(event, pageNo: number): void {
        this._from = (pageNo - 1) * this._step;
        this._fetchEntries();
    }
}
