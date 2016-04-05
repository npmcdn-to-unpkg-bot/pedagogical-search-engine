import {Injectable} from 'angular2/core'
import {Http} from 'angular2/http'

@Injectable()
export class CompletionService {
    constructor(private _http: Http) {

    }

    list() {
        return ['entry 1', 'entry 2', 'entry 3'];
    }
}
