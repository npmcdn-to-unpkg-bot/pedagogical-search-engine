import {Resource} from "./resource";

export class Completion {
    private _propositions: Array<Resource>

    constructor(propositions: Array<Resource> = []) {
        this._propositions = propositions;
    }

    // Public
    public update(newPropositions: Array<Resource>) {
        this.clear();
        this._propositions = newPropositions;
    }
    public getPropositions() {
        return this._propositions;
    }
    public nbOfPropositions() {
        return this._propositions.length;
    }
    public hasPropositions() {
        return (this.nbOfPropositions() > 0);
    }
    public clear() {
        delete this._propositions; // help GC
    }
}
