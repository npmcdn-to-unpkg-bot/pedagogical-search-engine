import {Result} from "./result";

export class RawSearch extends Result{
    constructor(public label: String,
                public uri: String) {
    }

    public isRawSearch(): boolean {
        return true;
    }

    public asRawSearch(): RawSearch {
        return this;
    }
}
