import {Observable} from "rxjs/Rx";
import {OpaqueToken} from "angular2/core";
import {Completion} from "./completion";

export const CompletionService = new OpaqueToken("CompletionService");

export interface CompletionService {
    list(text: String): Observable<Completion>
}
