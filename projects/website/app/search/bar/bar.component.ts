import {Component, ViewChild, Output, EventEmitter, Inject} from "angular2/core";
import {Router, RouteParams} from "angular2/router";
import {CompletionCmp} from "./completion/completion.component";
import {SearchTerm} from "../search-terms/SearchTerm";
import {Entity} from "./completion/result/entity";
import {AngularFixService} from "../../utils/angular-fix.service";

enum keys {Tab, Enter, Down, Up, Escape}

@Component({
    selector: 'wc-search-bar',
    template: `

<div>
    Search online Books & Courses
</div>

<div>
    <div class="wc-com-search-bar-input-wrapper">
    
        <input type="text" [(ngModel)]="_text"
            class="wc-com-search-bar-input"
            (keydown.tab)="_specialKeydown($event, _KEYS.Tab)"
            (keydown.enter)="_specialKeydown($event, _KEYS.Enter)"
            (keydown.ArrowDown)="_specialKeydown($event, _KEYS.Down)"
            (keydown.ArrowUp)="_specialKeydown($event, _KEYS.Up)"
            (keydown.Escape)="_specialKeydown($event, _KEYS.Escape)"
            #inputObj>
        
        <div class="wc-com-search-bar-input-bottom-container">
            <wc-completion
                class="wc-com-search-bar-suggestions"
                #completionObj
                [text]="_text"
                (itemSelected)="_itemSelected($event)">
            </wc-completion>
        </div>
    </div>
        
    <span class="wc-com-search-bar-st"
          *ngFor="#resource of _entities; #i = index">
        <span [textContent]="resource.label"></span>
        <span class="wc-com-search-bar-st-close"
              (click)="_remove(i)">&#x2715;</span>
    </span>
</div>

    `,
    directives: [CompletionCmp]
})
export class SearchBarCmp {
    private _text: String = '';
    private _entities: Array<Entity> = [];

    private _KEYS = keys;

    @Output('searchTermsChange') private _stcEmitter
        : EventEmitter<Array<SearchTerm>>
        = new EventEmitter();

    @ViewChild('completionObj') private _completionObj;
    @ViewChild('inputObj') private _input;

    // Init logic
    constructor(@Inject(Window) private _window: Window,
                @Inject(Router) private _router: Router,
                @Inject(RouteParams) private _routeParams: RouteParams,
                @Inject(AngularFixService) private _angularFixes: AngularFixService) {
        // Load url search-terms
        let q = this._routeParams.get('q');
        if(q) {
            let searchTerms = JSON.parse(decodeURIComponent(q));
            if(searchTerms.length > 0) {
                for(let searchTerm of searchTerms) {
                    this._entities.push(new Entity(searchTerm.label, undefined, searchTerm.uri, undefined));
                }
            }
        }
    }

    // Life-cycle hooks
    ngAfterViewInit(): void {
        let input = this._input;
        this._window.addEventListener("keydown", function(event){
            if(!event.ctrlKey) {
                input.nativeElement.focus();
            }
        });
    }
    ngAfterContentInit() {
        this._stcEmitter.emit(this._entities);
    }

    // Private
    private _reflectChangesinUrl() {
        // Compute the url-encoded object
        let sts = [];
        for(let e of this._entities) {
            sts.push(new SearchTerm(e.label, e.uri));
        }
        let searchParameters = this._angularFixes.encodeURIComponent(
            JSON.stringify(sts)
        );

         // Reflect in the url
        let params = {
            q: searchParameters
        };
        if('filter' in this._routeParams.params) {
            params['filter'] = this._routeParams.get("filter");
        }

         this._router.navigate(['Search', params]);
    }
    private _itemSelected(entity: Entity) {
        if(this._entities.indexOf(entity) === -1) {
            this._entities.push(entity);

            this._reflectChangesinUrl();
        }
        this._text = '';
    }

    private _asSearchTerm(entity: Entity): SearchTerm {
        return ;
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
        this._entities.splice(i, 1);
        this._reflectChangesinUrl();
    }
}