
import {Filter} from "./Filter";
export class NbResults {
    constructor(mapping = undefined) {
        if(mapping) {
            this._map = mapping;
        } else {
            this.set(Filter.all, 0);
            this.set(Filter.free, 0);
            this.set(Filter.paid, 0);
        }
    }

    private _map = {};

    public set(filter: Filter, n: number): void {
        this._map[Filter[filter]] = n;
    }

    public get(filter: Filter): number {
        if(Filter[filter] in this._map) {
            return this._map[Filter[filter]];
        } else {
            return undefined;
        }
    }
}