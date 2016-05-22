import {Component, Input, provide, Inject, SimpleChange} from "angular2/core";
import {SearchTerm} from "../search-terms/SearchTerm";
import {EntriesService} from "./entries.service";
import {Entry} from "./entry";
import {SimpleEntriesService} from "./simple-entries.service";
import {Router, RouteParams} from "angular2/router";
import {ClickService} from "../user-study/click.service";
import {SimpleClickService} from "../user-study/simple-click.service";
import {Response as HttpResponse} from "angular2/http";
import {Response} from "./response";
import {Classification} from "./classification";
import {ClassificationService} from "../user-study/classification.service";
import {Observable} from "rxjs/Observable";
import {HighlightService} from "../../utils/highlight.service";
import {WordsService} from "../../utils/words.service";
import {LineHighlightService} from "./line-highlight.service";
import {Filter} from "./Filter";
import {HelperService} from "../../helper/helper.service";
import {Q4Cmp} from "../../feedback/questions/q4.component";
import {UserstudyService} from "../../userstudy/userstudy";

@Component({
    selector: 'wc-search-results',
    template: `
    
<div class="wc-com-results-container">
    
    <div class="wc-com-results-tab-container"
         *ngIf="_searchTerms?.length > 0 && (_nbResults(_freeValue) > 0 || _nbResults(_paidValue) > 0)">
        <div class="wc-com-results-tab-link"
             (mousedown)="_navigateToFilter(_freeValue)">
            <span [class.wc-com-results-tab-link-selected]="_filter == _freeValue"
                  *ngIf="_nbResults(_freeValue) > 0">
                <span *ngIf="_filter == _freeValue">&#187;</span>
                Online courses 
                (<span [textContent]="_nbResults(_freeValue)"></span>)
            </span>
            <span *ngIf="_nbResults(_freeValue) == 0"
                  class="wc-com-results-tab-link-deactivated">
                <span *ngIf="_filter == _freeValue">&#187;</span>
                No Online courses 
            </span>
        </div>
        
        <div class="wc-com-results-tab-link"
             (mousedown)="_navigateToFilter(_paidValue)">
            <span [class.wc-com-results-tab-link-selected]="_filter == _paidValue"
                  *ngIf="_nbResults(_paidValue) > 0">
                <span *ngIf="_filter == _paidValue">&#187;</span>
                Books
                (<span [textContent]="_nbResults(_paidValue)"></span>)
            </span>
            <span *ngIf="_nbResults(_paidValue) == 0"
                  class="wc-com-results-tab-link-deactivated">
                <span *ngIf="_filter == _paidValue">&#187;</span>
                No Books
            </span>
        </div>
    </div>
    
    <h4 *ngIf="_searchTerms?.length > 0 && _isLoading">
        Loading…
    </h4>

    <div class="wc-com-results-entry"
        *ngFor="#entry of _response?.entries">
        <div>
            <span *ngIf="entry.isHighQuality()"
                  class="wc-com-colors-codes-good">
                Best Match &#187;
            </span>
            
            <a *ngIf="!entry.hasHref()"
               target="_blank"
                [href]="entry.epflHref()"
               (mousedown)="_logClick(entry)">
                <b [textContent]="entry.title"></b>
            </a>
            <a *ngIf="entry.hasHref()"
               target="_blank"
                class="wc-com-results-link wc-com-results-link-ok"
                [href]="entry.href"
               (mousedown)="_logClick(entry)">
                <b [textContent]="entry.title"></b>
            </a>
            
            <span class="wc-com-results-link-source" 
                  [textContent]="entry.typeText"></span>
        </div>
        <div class="wc-com-results-snippet-container"
             *ngFor="#line of entry.snippet.lines">
            <div class="wc-com-results-snippet-line"
                 [innerHTML]="_lineHService.highlight(line)"></div>
        </div>
        <div class="wc-com-results-topics-container"
             *ngIf="entry.topUris.length">
            <span class="wc-com-results-topics-text">
                Topics &#187;
            </span>
            <span class="wc-com-results-topics-entry"
                  [textContent]="_topUrisToStr(entry.topUris)"></span>
        </div>
        <div class="wc-com-results-rating-container">
            <wc-feedback-q4 text="How useful is this result?"
                            id="Q4-entry"
                            [inline]="true"
                            [supplement]="_supplementOf(entry)"
                            [supplementHash]="_supplementHashOf(entry)"></wc-feedback-q4>
        </div>
    </div>
    
    <div class="wc-com-results-pagination"
         *ngIf="_hasMultiplePages()">
        <span class="wc-com-results-pagination-text">
            Pages
        </span>

        <span class="wc-com-results-pagination-entry"
              *ngFor="#pageNo of _pages()">
              
            <span *ngIf="_isCurrentPage(pageNo)"
                    class="wc-com-results-pagination-entry-current">
                &#187; {{ pageNo }} &#171;
            </span>
                    
            <a *ngIf="!_isCurrentPage(pageNo)"
                (click)="_goPage($event, pageNo)"
                class="wc-com-results-pagination-entry-link"
                >
                {{ pageNo }}
            </a>
            
        </span>
    </div>
    
</div>

    
    `,
    directives: [Q4Cmp],
    providers: [
        provide(EntriesService, {useClass: SimpleEntriesService}),
        provide(ClickService, {useClass: SimpleClickService}),
        provide(LineHighlightService, {useClass: LineHighlightService}),
        provide(HighlightService, {useClass: HighlightService}),
        provide(WordsService, {useClass: WordsService})
    ]
})
export class ResultsCmp {

    @Input('searchTerms') private _searchTerms: Array<SearchTerm> = [];

    private _response: Response;
    private _filter: Filter = Filter.free;
    private _freeValue: Filter = Filter.free;
    private _paidValue: Filter = Filter.paid;
    private _from: number = 0;
    private _step: number = 10;
    private _irrelevant: Classification = Classification.irrelevant;
    private _irlvunselect: Classification = Classification.irlvunselect;
    private _isLoading: boolean = true;

    constructor(
        @Inject(EntriesService) private _entriesService: EntriesService,
        @Inject(ClickService) private _clickService: ClickService,
        @Inject(ClassificationService) private _clsService: ClassificationService,
        @Inject(LineHighlightService) private _lineHService: LineHighlightService,
        @Inject(UserstudyService) private _usService: UserstudyService,
        private _router: Router,
        private _routeParams: RouteParams,
        private _helper: HelperService
    ) {
        // Is there any page-no in the url?
        let pageNo = +_routeParams.get('page');
        if(pageNo > 0) {
            this._from = (pageNo - 1) * this._step;
        }

        // Is there any filter in the url?
        let filter = _routeParams.get('filter');
        if(filter) {
            this._filter = Filter[filter];
        }
    }

    // Life-cycle hooks
    ngOnChanges(changes: {_searchTerms: SimpleChange}) {
        let sts = changes._searchTerms.currentValue;
        if(sts && sts.length > 0) {
            this._fetchEntries();
        }
    }

    // Private
    private _nbResults(filter: Filter): number {
        if(this._response && this._response.nbResults && this._response.nbResults.get(filter)) {
            return this._response.nbResults.get(filter);
        } else {
            return 0;
        }
    }
    private _navigateToFilter(filter: Filter) {
        this._navigate(filter, 1);
    }
    private _navigateToPage(pageNo: number): void {
        this._navigate(undefined, pageNo);
    }
    private _navigate(pFilter: Filter = undefined,
                      pPageNo: number = undefined) {
        let newParams = {
            q: this._routeParams.get("q")
        };

        if("filter" in this._routeParams.params || pFilter) {
            newParams["filter"] = pFilter? Filter[pFilter]: this._routeParams.get("filter");
        }
        if("page" in this._routeParams.params || pPageNo) {
            newParams["page"] = pPageNo? pPageNo: this._routeParams.get("page");
        }
        this._router.navigate(['Search', newParams]);
    }
    private _classify(entry: Entry, classification: Classification): void {
        // Log the classification
        let stream: Observable<any> = this._clsService.saveClassification(
            this._searchTerms,
            entry.entryId,
            classification
        );

        stream.subscribe(res => {
            console.log(res.text());
        });
    }
    private _logClick(entry: Entry): void {
        // Log the click
        let clickStream = this._clickService.saveClick(
            this._searchTerms,
            entry.entryId,
            this._from + entry.rank,
            entry.quality);

        clickStream.subscribe((res: HttpResponse) => {
            console.log(`Click logged: ${res.text()}`);
        });
    }
    private _isCurrentPage(pageNo: number): boolean {
        let currentPage = (this._from / this._step) + 1;
        return (pageNo == currentPage);
    }
    private _fetchEntries(): void {
        if(this._searchTerms.length > 0) {
            // Log the fetch
            let sts = "";
            for(let st of this._searchTerms) {
                if(sts.length > 0) {
                    sts = `${sts}, ${st.uri}`;
                } else {
                    sts = st.uri;
                }
            }
            let pageNo = Math.floor(this._from / this._step) + 1;
            console.log(`fetch ${this._step} entries of {${sts}} from ${this._from} (page ${pageNo})`);

            // Perform the fetch
            this._isLoading = true;
            let entriesObs = this._entriesService.list(
                this._searchTerms,
                this._from,
                this._to(),
                this._filter
            );
            entriesObs.subscribe(res => {
                this._isLoading = false;

                // Debug case: there are no results
                if(res.entries.length > 0) {
                    this._response = res;
                } else {
                    // Navigate to page 1
                    this._navigateToPage(1);
                }
            });
        }
    }
    private _hasMultiplePages(): boolean {
        if(this._response) {
            return this._response.countResults(this._filter) > this._step;
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
            return Math.ceil(this._response.countResults(this._filter) / this._step);
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
        // Stop the event
        event.preventDefault();
        event.stopPropagation();

        this._navigateToPage(pageNo);
    }
    private _topUrisToStr(uris: Array<string>)
    : string {
        let acc = "";
        let join = ", ";
        let m = 85;
        for(let uri of uris) {
            if(acc.length + uri.length <= m) {
                if(acc.length > 0) {
                    acc += ", " + uri;
                } else {
                    acc += uri;
                }
            }
        }
        return acc;
    }
    private _supplementOf(entry: Entry)
    : any {
        return {
            'searchLog': JSON.stringify(this._searchTerms),
            'sid': this._usService.sid,
            'entryId': entry.entryId
        };
    }
    private _supplementHashOf(entry: Entry)
    : string {
        // Javascript absence of hash function..
        // We use stringify in replacement
        return JSON.stringify(this._supplementOf(entry));
    }
}
