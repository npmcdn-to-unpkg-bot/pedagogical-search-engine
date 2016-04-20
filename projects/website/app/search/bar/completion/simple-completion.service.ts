import {Injectable, Inject} from "angular2/core";
import {Http, Headers, RequestOptions} from "angular2/http";
import {Observable} from "rxjs/Rx";
import "rxjs/Rx";
import {Completion} from "./completion";
import {CompletionService} from "./completion.service";
import {Entity} from "./result/entity";
import {Disambiguation} from "./result/disambiguation";

@Injectable()
export class SimpleCompletionService extends CompletionService {
    constructor(private _http: Http, @Inject('SETTINGS') private _settings) {}

    public list(text: String): Observable<Completion> {
        let url = this._settings.AUTOCOMPLETE_URL;
        let headers = new Headers({ 'Content-Type': 'application/json' });
        let options = new RequestOptions({ headers: headers });
        let body = JSON.stringify({
            "text": text
        });

        return this._http.post(url, body, options)
            .map(res => {
                let json = res.json();
                let results = [];
                for(let e of json) {
                    let label = e["label"];
                    let uri = e["uri"];
                    let disambiguating = e["disambiguating"];
                    if(disambiguating.length > 0) {
                        let disambiguations = [];
                        for(let e2 of disambiguating) {
                            let label2 = e2["label"];
                            let uri2 = e2["uri"];
                            disambiguations.push(new Entity(label2, uri2));
                        }
                        results.push(new Disambiguation(label, uri, disambiguations));
                    } else {
                        results.push(new Entity(label, uri));
                    }
                }

                return new Completion(results);
            })
            .catch(res => {
                console.log("Error with autocompletion:");
                console.log(res);
            });
    }
}
