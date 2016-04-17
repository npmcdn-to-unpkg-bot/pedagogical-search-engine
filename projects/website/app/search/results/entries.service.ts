import {Injectable, OpaqueToken} from "angular2/core";
import {Observable} from "rxjs/Observable";
import {Entry} from "./entry";
import {SearchTerm} from "../search-terms/SearchTerm";

export const EntriesService = new OpaqueToken("EntriesService");

export interface EntriesService {
    list(searchTerms: Array<SearchTerm>): Observable<Array<Entry>>
}