import {Injectable, Inject} from "angular2/core";
import {MessageService} from "../message.service";
import {Observable} from "rxjs/Observable";
import {LocalStorageService} from "../utils/LocalStorageEmitter";
import {LocalStorage} from "../utils/WebStorage";

@Injectable()
export class HelperService {
    constructor(storageService: LocalStorageService) {
    }

    @LocalStorage() private _cache = {};

    // Private methods
    
    // Public methods
    public hasBeenDisplayed(key: string)
    : boolean {
        return (key in this._cache);
    }
    public setAsDisplayed(key: string)
    : boolean {
        return this._cache[key] = true;
    }
}