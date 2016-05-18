import {Injectable, Inject} from "angular2/core";
import {UserstudyService} from "../userstudy/userstudy";
import {MessageService} from "../message.service";
import {Observable} from "rxjs/Observable";
import {Response} from "angular2/http";

@Injectable()
export class FeedbackService {
    constructor(@Inject(MessageService) private _msService: MessageService) {}
    
    private _cache = {};

    // Private methods
    private static _jsonRep(id: string, value: string): string {
        return JSON.stringify({
            "questionId": id,
            "value": value
        })
    }
    
    // Public methods
    public hasBeenAnswered(id: string)
    : boolean {
        return (id in this._cache);
    }
    
    public getAnswer(id: string)
    : string {
        if(this.hasBeenAnswered(id)) {
            return this._cache[id];
        } else {
            return "";
        }
    }
    
    public saveAnswer(id: string, value: string)
    : Observable<any> {
        this._cache[id] = value;

        return this._msService.log(
            "feedback",
            FeedbackService._jsonRep(id, value));
    }
}