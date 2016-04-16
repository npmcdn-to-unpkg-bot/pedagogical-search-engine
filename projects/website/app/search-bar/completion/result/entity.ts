import {Result} from "./result";

export class Entity extends Result {
    constructor(public label: String, public uri: String) {}
    
    public isEntity(): boolean {
        return true;
    }

    public asEntity(): Entity {
        return this;
    }
}