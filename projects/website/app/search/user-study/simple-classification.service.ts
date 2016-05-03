import {Injectable, Inject} from "angular2/core";
import {Http, Headers, RequestOptions} from "angular2/http";
import {SearchTerm} from "../search-terms/SearchTerm";
import {ClassificationService} from "./classification.service";
import {Classification} from "../results/classification";

@Injectable()
export class SimpleClassificationService extends ClassificationService {
    constructor(
        private _http: Http,
        @Inject('SETTINGS') private _settings
    ) {}

    // Public method
    public saveClassification(
        searchTerms:Array<SearchTerm>,
        entryId: String,
        classification: Classification
    ) {
        // Extract the uris
        let uris: Array<String> = [];
        for(let st of searchTerms) {
            uris.push(st.uri);
        }

        // Save the click
        let url = this._settings.STUDY_URL + "/classifications";

        let headers = new Headers({ 'Content-Type': 'application/json' });
        let options = new RequestOptions({ headers: headers });
        let body = JSON.stringify({
            "uris": uris,
            "entryId": entryId,
            "classification": Classification[classification]
        });

        return this._http.post(url, body, options);
    }
}