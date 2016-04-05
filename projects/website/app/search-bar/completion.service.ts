import {Completion} from "./completion";
import {Observable} from "rxjs/Rx";
import {OpaqueToken} from "angular2/core";

export const CompletionService = new OpaqueToken("CompletionService");

export interface CompletionService {
    list(): Observable<Completion>
}
