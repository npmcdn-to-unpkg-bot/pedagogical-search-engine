import {Injectable, Inject} from "angular2/core";
import {MessageService} from "../message.service";
import {Observable} from "rxjs/Observable";
import {LocalStorageService} from "../utils/LocalStorageEmitter";
import {LocalStorage} from "../utils/WebStorage";

@Injectable()
export class FeedbackService {
    constructor(@Inject(MessageService) private _msService: MessageService,
                storageService: LocalStorageService) {}

    @LocalStorage() private _cache = {};

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
    : any {
        if(this.hasBeenAnswered(id)) {
            return this._cache[id];
        } else {
            return "";
        }
    }
    
    public saveAnswer(id: string, value: any)
    : Observable<any> {
        this._cache[id] = value;

        return this._msService.log(
            "feedback",
            FeedbackService._jsonRep(id, value));
    }

    public hasBeenAnswered2(id: string, id2: string)
    : boolean {
        return (id in this._cache && id2 in this._cache[id]);
    }

    public getAnswer2(id: string, id2: string)
    : any {
        if(this.hasBeenAnswered2(id, id2)) {
            return this._cache[id][id2];
        } else {
            return "";
        }
    }

    public saveAnswer2(id: string, id2: string, value: any)
    : Observable<any> {
        if(!(id in this._cache)) {
            this._cache[id] = {};
        }
        this._cache[id][id2] = value;

        return this._msService.log(
            "feedback",
            FeedbackService._jsonRep(id, value));
    }
}