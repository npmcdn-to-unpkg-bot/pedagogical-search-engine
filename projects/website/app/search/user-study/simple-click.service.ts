import {Injectable, Inject} from "angular2/core";
import {Http, Headers, RequestOptions} from "angular2/http";
import {SearchTerm} from "../search-terms/SearchTerm";
import {Quality} from "../results/quality";
import {ClickService} from "./click.service";

@Injectable()
export class SimpleClickService extends ClickService {
    constructor(
        private _http: Http,
        @Inject('SETTINGS') private _settings
    ) {}

    // Public method
    public saveClick(
        searchTerms:Array<SearchTerm>,
        entryId: String,
        rank: number,
        quality: Quality
    ) {
        // Extract the uris
        let uris: Array<String> = [];
        for(let st of searchTerms) {
            uris.push(st.uri);
        }


        // Save the click
        let url = this._settings.STUDY_URL + "/clicks";

        let headers = new Headers({ 'Content-Type': 'application/json' });
        let options = new RequestOptions({ headers: headers });
        let body = JSON.stringify({
            "uris": uris,
            "entryId": entryId,
            "rank": rank,
            "quality": Quality[quality]
        });

        return this._http.post(url, body, options);
    }
}