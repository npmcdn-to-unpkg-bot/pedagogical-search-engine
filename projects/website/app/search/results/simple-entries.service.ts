import {EntriesService} from "./entries.service";
import {Entry} from "./entry";
import {Observable} from "rxjs/Observable";
import {SearchTerm} from "../search-terms/SearchTerm";
import {Injectable, Inject} from "angular2/core";
import {Http, Headers, RequestOptions} from "angular2/http";
import {Snippet} from "./snippet";
import {Spot} from "./spot";
import {Line} from "./line";

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

                // Extract the entries
                let entries: Array<Entry> = [];
                for(let e of json) {
                    // Extract basic information
                    let title = e["title"];
                    let typeText = e["typeText"];
                    let href = e["href"];
                    let score = e["score"];
                    let snippetStr = e["snippet"];

                    // Is there any snippet
                    let snippet;
                    if(snippetStr.length == 0) {
                        snippet = new Snippet();
                    } else {
                        // Extract the snippet
                        let snippetJson = JSON.parse(snippetStr);

                        let lines = [];
                        for(let s1 of snippetJson) {
                            let text = s1["text"];
                            let spots = [];
                            for(let s2 of s1["spots"]) {
                                spots.push(new Spot(
                                    s2["start"],
                                    s2["stop"],
                                    s2["uri"]
                                ));
                            }
                            lines.push(new Line(text, spots));
                        }

                        snippet = new Snippet(lines);
                    }

                    let entry = new Entry(title, typeText, href, snippet, score);
                    entries.push(entry);
                }
                let ordered = entries.sort((a: Entry, b: Entry) => {
                    if(a.score < b.score) {
                        return 1;
                    } else if(a.score > b.score) {
                        return -1;
                    } else {
                        return 0;
                    }
                })

                return entries;
            })
            .catch(res => {
                console.log("Error with entries-service:");
                console.log(res);
            });
    }
}