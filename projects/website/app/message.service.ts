import {Injectable, Inject} from "angular2/core";
import {Http, Headers, RequestOptions, Response} from "angular2/http";
import {UserstudyService} from "./userstudy/userstudy";

@Injectable()
export class MessageService {

    constructor(
        private _http: Http,
        @Inject('SETTINGS') private _settings,
        @Inject(UserstudyService) private _usService: UserstudyService
    ) {}

    public log(category: string, content: string): any {
        // Save the click
        let url = this._settings.STUDY_URL + "/messages";

        let headers = new Headers({ 'Content-Type': 'application/json' });
        let options = new RequestOptions({ headers: headers });
        let body = JSON.stringify({
            "sid": this._usService.sid,
            "category": category,
            "content": content
        });

        return this._http.post(url, body, options).map((res: Response) => {
            res.statusText = `Logged message (${category}) of length ${content.length}`;
            return res;
        });
    }
}