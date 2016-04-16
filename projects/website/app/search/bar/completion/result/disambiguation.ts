import {Entity} from "./entity";
import {Result} from "./result";

export class Disambiguation extends Result{
    constructor(public label: String,
                public uri: String,
                public entities: Array<Entity>) {
    }

    public isDisambiguation(): boolean {
        return true;
    }

    public asDisambiguation(): Disambiguation {
        return this;
    }
}
