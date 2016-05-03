import {Injectable, OpaqueToken} from "angular2/core";
import {SearchTerm} from "../search-terms/SearchTerm";
import {Classification} from "../results/classification";

export const ClassificationService = new OpaqueToken("ClassificationService");

@Injectable()
export interface ClassificationService {
    saveClassification(
        searchTerms:Array<SearchTerm>,
        entryId: String,
        classification: Classification
    )
}