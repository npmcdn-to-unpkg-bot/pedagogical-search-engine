import {Entity} from "./entity";
import {Disambiguation} from "./disambiguation";
import {RawSearch} from "./rawSearch";

export abstract class Result {


    protected abstract _displayLabelImpl(): string

    public displayLabel(): string {
        let full = this._displayLabelImpl();
        let skimmed = full.substring(0, 48);
        if(full.length !== skimmed.length) {
            return skimmed + "..";
        } else {
            return full;
        }
    }

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
