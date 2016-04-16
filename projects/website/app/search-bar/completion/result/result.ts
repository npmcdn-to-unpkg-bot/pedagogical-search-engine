import {Entity} from "./entity";
import {Disambiguation} from "./disambiguation";

export abstract class Result {
    public isEntity(): Boolean {
        return !this.isDisambiguation();
    }

    public isDisambiguation(): Boolean {
        return !this.isEntity();
    }

    public asEntity(): Entity {
        return null;
    }
    public asDisambiguation(): Disambiguation {
        return null;
    }
}
