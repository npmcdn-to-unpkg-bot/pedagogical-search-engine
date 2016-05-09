import {Entity} from "./entity";
import {Disambiguation} from "./disambiguation";
import {RawSearch} from "./rawSearch";

export abstract class Result {
    public isEntity(): Boolean {
        return false;
    }

    public isDisambiguation(): Boolean {
        return false;
    }

    public isRawSearch(): Boolean {
        return false;
    }

    public asEntity(): Entity {
        return null;
    }
    public asDisambiguation(): Disambiguation {
        return null;
    }
    public asRawSearch(): RawSearch {
        return null;
    }
}
