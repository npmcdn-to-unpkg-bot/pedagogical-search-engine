import {Proposition} from "./proposition";
import {Resource} from "./resource";

export class Completion {
    private _propositions: Array<Proposition> = [];

    constructor(resources: Array<Resource> = []) {
        for(let res of resources) {
            this._propositions.push(new Proposition(res, false))
        }
    }

    // Public
    public update(newPropositions: Array<Proposition>): void {
        this.clear();
        this._propositions = newPropositions;
    }
    public getPropositions(): Array<Proposition> {
        return this._propositions;
    }
    public getProposition(index: number): Proposition {
        return this._propositions[index];
    }
    public nbOfPropositions(): number {
        return this._propositions.length;
    }
    public hasPropositions(): boolean {
        return (this.nbOfPropositions() > 0);
    }
    public clear(): void {
        delete this._propositions; // help GC
    }
    public select(index): void {
        for(let i = 0; i < this._propositions.length; i++) {
            if(i === index) {
                this._propositions[i].select();
            } else {
                this._propositions[i].unselect();
            }
        }
    }
}
