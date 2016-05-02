import {Injectable, OpaqueToken} from "angular2/core";
import {SearchTerm} from "../search-terms/SearchTerm";
import {Quality} from "../results/quality";

export const ClickService = new OpaqueToken("ClickService");

@Injectable()
export interface ClickService {
    saveClick(
        searchTerms:Array<SearchTerm>,
        entryId: String,
        rank: number,
        quality: Quality
    )
}