import {Injectable, OpaqueToken} from "angular2/core";
import {Observable} from "rxjs/Observable";
import {SearchTerm} from "../search-terms/SearchTerm";
import {Response} from "./response";
import {Filter} from "./Filter";

export const EntriesService = new OpaqueToken("EntriesService");

@Injectable()
export interface EntriesService {
    list(searchTerms: Array<SearchTerm>,
        from: number,
        to: number,
        filter: Filter): Observable<Response>
}