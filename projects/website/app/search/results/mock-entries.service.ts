import {EntriesService} from "./entries.service";
import {Entry} from "./entry";
import {Observable} from "rxjs/Observable";
import {SearchTerm} from "../search-terms/SearchTerm";
import {Injectable} from "angular2/core";
import {Response} from "./response";
import {Snippet} from "./snippet";
import {Line} from "./line";
import {Quality} from "./quality";

@Injectable()
export class MockEntriesService extends EntriesService {

    public latencyMs = 1500;

    constructor(){}

    list(searchTerms:Array<SearchTerm>, from: number, to: number):Observable<Response> {
        let entries = [];
        for(let i = from; i <= to; i++) {
            entries.push(this._newEntry());
        }
        let response = new Response(entries, 43);

        return Observable.of(response).delay(this.latencyMs);
    }

    private _newEntry(): Entry {
        let n = Math.ceil(Math.random() * 10);
        let title = `Entry ${n}`;
        let type = 'Book';
        let href = 'http://example.com';
        let snippet = 'Week 1: Chaos magnifico est.';
        let score = n / 10;
        let quality: Quality;
        if(score < 0.33) {
            quality = Quality.low;
        } else if(score < 0.99) {
            quality = Quality.medium;
        } else {
            quality = Quality.high;
        }
        return new Entry(title, type, href, new Snippet([new Line(snippet, [])]), quality, 1);
    }
}