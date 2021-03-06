import {Result} from "./result/result";
import {Proposition} from "./proposition";

export class Completion {
    private _propositions: Array<Proposition> = [];

    constructor(results: Array<Result> = []) {
        for(let res of results) {
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
        if(this._propositions) {
            return this._propositions.length;
        } else {
            return 0;
        }
    }
    public hasPropositions(): boolean {
        return (this.nbOfPropositions() > 0);
    }
    public clear(): void {
        delete this._propositions; // help GC
    }
    public select(index): void {
        if(this._propositions) {
            for(let i = 0; i < this._propositions.length; i++) {
                if(i === index) {
                    this._propositions[i].select();
                } else {
                    this._propositions[i].unselect();
                }
            }
        }
    }
    public selectedIndex(): number {
        if(this._propositions) {
            for(let i = 0; i < this._propositions.length; i++) {
                if(this._propositions[i].isSelected()) {
                    return i;
                }
            }
        }
        return -1;
    }
}
