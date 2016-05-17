import {EntriesService} from "./entries.service";
import {Entry} from "./entry";
import {Observable} from "rxjs/Observable";
import {SearchTerm} from "../search-terms/SearchTerm";
import {Injectable, Inject} from "angular2/core";
import {Http, Headers, RequestOptions} from "angular2/http";
import {Snippet} from "./snippet";
import {Spot} from "./spot";
import {Line} from "./line";
import {Response} from "./response";
import {Quality} from "./quality";
import {NbResults} from "./NbResults";
import {Filter} from "./Filter";
import {UserstudyService} from "../../userstudy/userstudy";

@Injectable()
export class SimpleEntriesService extends EntriesService {

    public latencyMs = 500;

    constructor(
        private _http: Http,
        @Inject('SETTINGS') private _settings,
        @Inject(UserstudyService) private _usService
    ){}

    list(searchTerms: Array<SearchTerm>, from: number, to: number, filter: Filter)
    :Observable<Response> {
        // Fetch the entries
        let url = this._settings.ENTRIES_URL;
        let headers = new Headers({ 'Content-Type': 'application/json' });
        let options = new RequestOptions({ headers: headers });
        let body = JSON.stringify({
            "searchTerms": SearchTerm.wsRepresentation(searchTerms),
            "from": from,
            "to": to,
            "filter": Filter[filter],
            "sid": this._usService.sid
        });

        return this._http.post(url, body, options)
            .map(res => {
                let json = res.json();

                // Check for entries
                let jsonEntries = json["entries"];
                if(jsonEntries.length == 0) {
                    return new Response([], new NbResults());
                }

                // Extract the entries
                let entries: Array<Entry> = [];
                for(let e of jsonEntries) {
                    // Extract basic information
                    let entryId = e["entryId"];
                    let title = e["title"];
                    let typeText = e["source"] + " - " + e["engine"];
                    let href = e["href"];
                    let rank: number = +e["rank"];
                    let quality = Quality[e["quality"]];
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

                    let entry = new Entry(entryId, title, typeText, href, snippet, quality, rank);
                    entries.push(entry);
                }
                let ordered = entries.sort((a: Entry, b: Entry) => {
                    return a.rank - b.rank;
                });

                return new Response(ordered, new NbResults(json["nbResults"]));
            })
            .catch(res => {
                console.log("Error with entries-service:");
                console.log(res);
            });
    }
}