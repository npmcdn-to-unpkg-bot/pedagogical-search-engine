import {Injectable} from "angular2/core";
import {LocalStorageService} from "../utils/LocalStorageEmitter";
import {LocalStorage} from "../utils/WebStorage";

@Injectable()
export class UserstudyService {
    constructor(storageService: LocalStorageService) {
        this._logId();
    }

    @LocalStorage() public _sid: number = this._gen();

    private _nullValue = -1;

    get sid(): number {
        return this.isDisabled()? undefined: this._sid;
    }

    public disable()
    : void {
        this._sid = this._nullValue;
        this._logId();
    }

    public enable()
    : void {
        this._sid = this._gen();
        this._logId();
    }

    public isDisabled()
    : boolean {
        return (this._sid == this._nullValue);
    }

    private _gen(): number {
        // Generate an session id used to track
        // the actions of a user anonymously throughout
        // his sessio n
        let million = 1000000;

        // Less than a 1% chance of have an id collision if
        // the study is on a million users.
        return Math.floor(Math.random() * million * 100);
    }

    private _logId()
    : void {
        if(this.isDisabled()) {
            console.log(`You do not have an anonymous id: Logging was disabled for you`);
        } else {
            console.log(`Your anonymous id is now '${this._sid}'`);
        }
    }
}