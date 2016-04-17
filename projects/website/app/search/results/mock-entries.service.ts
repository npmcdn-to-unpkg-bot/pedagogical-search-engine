import {EntriesService} from "./entries.service";
import {Entry} from "./entry";
import {Observable} from "rxjs/Observable";
import {SearchTerm} from "../search-terms/SearchTerm";
import {Injectable} from "angular2/core";

@Injectable()
export class MockEntriesService extends EntriesService {

    public latencyMs = 500;

    constructor(){}

    list(searchTerms:Array<SearchTerm>):Observable<Array<Entry>> {
        let entries = [
            this._newEntry(),
            this._newEntry(),
            this._newEntry(),
            this._newEntry(),
            this._newEntry(),
            this._newEntry(),
            this._newEntry()
        ];

        return Observable.of(entries).delay(this.latencyMs);
    }

    private _newEntry(): Entry {
        let n = Math.ceil(Math.random() * 10);
        let title = `Entry ${n}`;
        let type = 'Book';
        let href = 'http://example.com';
        let snippet = 'Week 1: Chaos magnifico est.';
        return new Entry(title, type, href, snippet);
    }
}