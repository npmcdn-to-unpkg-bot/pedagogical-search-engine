import {Injectable} from "angular2/core";
import {SearchTerm} from "./SearchTerm";

@Injectable()
export class FactoryService {
    constructor() {}

    public produce(label: String, uri: String): SearchTerm {
        return new SearchTerm(label, uri);
    }
}