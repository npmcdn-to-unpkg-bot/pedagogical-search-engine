import {Injectable} from "angular2/core";

@Injectable()
export class UserstudyService {
    constructor() {
        // Generate an session id used to track
        // the actions of a user anonymously throughout
        // his session
        let million = 1000000;

        // Less than a 1% chance of have an id collision if
        // the study is on a million users.
        this._sid = Math.floor(Math.random() * million * 100);
    }

    private _sid: number;

    get sid(): number {
        return this._sid;
    }
}