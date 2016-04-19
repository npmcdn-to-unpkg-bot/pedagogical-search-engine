import {Component, Input, provide, Inject, SimpleChange} from "angular2/core";
import {SearchTerm} from "../search-terms/SearchTerm";
import {EntriesService} from "./entries.service";
import {Entry} from "./entry";
import {SimpleEntriesService} from "./simple-entries.service";

@Component({
    selector: 'wc-search-results',
    template: `
    
    <div *ngFor="#entry of _entries" style="padding: 5px;">
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
    
    `,
    directives: [],
    providers: [
        provide(EntriesService, {useClass: SimpleEntriesService})
    ]
})
export class ResultsCmp {

    @Input('searchTerms') private _searchTerms: Array<SearchTerm> = [];

    private _entries: Array<Entry> = [];

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
        let entriesObs = this._entriesService.list(this._searchTerms);
        entriesObs.subscribe(res => {
            this._entries = res;
        });
    }
}
