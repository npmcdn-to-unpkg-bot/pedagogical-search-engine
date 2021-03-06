import {Result} from "./result/result";
import {Entity} from "./result/entity";
import {Disambiguation} from "./result/disambiguation";
import {RawSearch} from "./result/rawSearch";

export class Proposition {
    private _result: Result;
    private _isSelected: boolean;

    constructor(result: Result, isSelected: boolean) {
        this._result = result;
        this._isSelected = isSelected;
    }

    // Public
    public isSelected(): boolean {
        return this._isSelected;
    }
    public getResult(): Result {
        return this._result;
    }
    public select() {
        this._isSelected = true;
    }
    public unselect() {
        this._isSelected = false;
    }
}
