import {Result} from "./result";

export class Entity extends Result {
    constructor(public label: String,
                public hintLabel: String,
                public uri: String,
                public available: boolean) {}
    
    public isEntity(): boolean {
        return true;
    }

    public asEntity(): Entity {
        return this;
    }

    protected _displayLabelImpl(): string {
        return `${this.hintLabel}`;
    }
}