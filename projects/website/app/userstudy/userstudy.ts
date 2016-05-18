import {Injectable} from "angular2/core";
import {LocalStorageService} from "../utils/LocalStorageEmitter";
import {LocalStorage} from "../utils/WebStorage";

@Injectable()
export class UserstudyService {
    constructor(storageService: LocalStorageService) {
        console.log(`Your anonymous id is ${this.sid}`);
    }

    @LocalStorage() public sid: number = this._gen();

    private _gen(): number {
        // Generate an session id used to track
        // the actions of a user anonymously throughout
        // his sessio n
        let million = 1000000;

        // Less than a 1% chance of have an id collision if
        // the study is on a million users.
        return Math.floor(Math.random() * million * 100);
    }
}