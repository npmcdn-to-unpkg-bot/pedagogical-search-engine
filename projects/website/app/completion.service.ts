import {Injectable} from 'angular2/core'
import {Http} from 'angular2/http'
import {Observable} from "rxjs/Rx";

@Injectable()
export class CompletionService {
    constructor(private _http: Http) {

    }

    list(): Observable<Array<{label: String}>> {
        return Observable.of([
            {
                "label": 'entry 1'
            }, {
                "label": 'entry 2'
            }, {
                "label": 'entry 3'
            }]);
    }
}
