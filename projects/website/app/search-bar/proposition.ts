import {Resource} from "./resource";

export class Proposition {
    private _resource: Resource;
    private _isSelected: boolean;

    constructor(resource: Resource, isSelected: boolean) {
        this._resource = resource;
        this._isSelected = isSelected;
    }

    // Public
    public isSelected(): boolean {
        return this._isSelected;
    }
    public getResource(): Resource {
        return this._resource;
    }
    public select() {
        this._isSelected = true;
    }
    public unselect() {
        this._isSelected = false;
    }
}
