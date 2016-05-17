import {Injectable, Inject} from "angular2/core";
import {Http, Headers, RequestOptions, Response as HttpResponse} from "angular2/http";
import {SearchTerm} from "../search-terms/SearchTerm";
import {ClassificationService} from "./classification.service";
import {Classification} from "../results/classification";
import {Entry} from "../results/entry";
import {UserstudyService} from "../../userstudy/userstudy";
import privateMemberModifier = ts.ScriptElementKindModifier.privateMemberModifier;

@Injectable()
export class SimpleClassificationService extends ClassificationService {
    constructor(
        private _http: Http,
        @Inject('SETTINGS') private _settings,
        @Inject(UserstudyService) private _usService
    ) {}

    private _cache: Array<Classification> = [];
    private _msgCache: Array<String> = [];
    private _thxMsgs: Array<String> = [
        "Thank you for your feedback",
        "",
        "Thanks again",
        "", "", "", "",
        "Hey thanks! Your feedback helps us a lot :)"
    ];

    // Private method
    private _possibleThanksMsg(): String {
        if(this.nbClassifications() < this._thxMsgs.length) {
            return this._thxMsgs[this.nbClassifications()];
        } else {
            return "";
        }
    }

    // Public method
    public saveClassification(
        searchTerms:Array<SearchTerm>,
        entryId: String,
        classification: Classification
    ) {
        if(!(entryId in this._msgCache)) {
            this._msgCache[entryId] = this._possibleThanksMsg();
        }
        if(entryId in this._cache) {
            if(classification === Classification.relevant) {
                classification = Classification.rlvpatch;
            } else {
                classification = Classification.irlvpatch;
            }
        }
        this._cache[entryId] = classification;

        // Save the click
        let url = this._settings.STUDY_URL + "/classifications";

        let headers = new Headers({ 'Content-Type': 'application/json' });
        let options = new RequestOptions({ headers: headers });
        let body = JSON.stringify({
            "searchTerms": SearchTerm.wsRepresentation(searchTerms),
            "entryId": entryId,
            "classification": Classification[classification],
            "sid": this._usService.sid
        });

        return this._http.post(url, body, options).map((res: HttpResponse) => {
            res.statusText = `Logged ${classification}, server responded with ${res.text()}`;
            return res;
        });
    }

    public nbClassifications(): number {
        return Object.keys(this._cache).length;
    }


    public isClassified(entry: Entry): boolean {
        return (entry.entryId in this._cache);
    }
    public isRelevant(entry: Entry): boolean {
        return this.isClassified(entry) &&
            (this._cache[entry.entryId] === Classification.relevant ||
            this._cache[entry.entryId] === Classification.rlvpatch);
    }
    public isIrrelevant(entry: Entry): boolean {
        return this.isClassified(entry) &&
            (this._cache[entry.entryId] === Classification.irrelevant ||
            this._cache[entry.entryId] === Classification.irlvpatch);
    }
    public thxMsg(entry: Entry): String {
        return (entry.entryId in this._msgCache)? this._msgCache[entry.entryId]: '';
    }

}