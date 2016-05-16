import {Entry} from "./entry";
import {NbResults} from "./NbResults";
import {Filter} from "./Filter";

export class Response {
    constructor(public entries: Array<Entry>,
                public nbResults: NbResults = new NbResults()){}

    public countResults(filter: Filter = Filter.all): number {
        return this.nbResults.get(filter);
    }
}