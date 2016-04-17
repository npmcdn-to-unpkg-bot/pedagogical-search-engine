import {EntriesService} from "./entries.service";
import {Entry} from "./entry";
import {Observable} from "rxjs/Observable";
import {SearchTerm} from "../search-terms/SearchTerm";
import {Injectable, Inject} from "angular2/core";
import {Http, Headers, RequestOptions} from "angular2/http";

@Injectable()
export class SimpleEntriesService extends EntriesService {

    public latencyMs = 500;

    constructor(
        private _http: Http,
        @Inject('SETTINGS') private _settings
    ){}

    list(searchTerms:Array<SearchTerm>):Observable<Array<Entry>> {
        // Extract the uris
        let uris: Array<String> = [];
        for(let st of searchTerms) {
            uris.push(st.uri);
        }

        // Fetch the entries
        let url = this._settings.ENTRIES_URL;
        let headers = new Headers({ 'Content-Type': 'application/json' });
        let options = new RequestOptions({ headers: headers });
        let body = JSON.stringify({
            "uris": uris
        });

        return this._http.post(url, body, options)
            .map(res => {
                let json = res.json();

                // todo: Extract the entries
                let entries: Array<Entry> = [];

                return entries;
            })
            .catch(res => {
                console.log("Error with entries-service:");
                console.log(res);
            });
    }
}