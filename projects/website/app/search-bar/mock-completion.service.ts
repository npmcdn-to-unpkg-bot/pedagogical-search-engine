import {Injectable} from "angular2/core";
import {Http} from "angular2/http";
import {Observable} from "rxjs/Rx";
import {Completion} from "./completion";
import {Resource} from "./resource";
import {CompletionService} from "./completion.service";

@Injectable()
export class MockCompletionService extends CompletionService {
    constructor(private _http: Http) {}

    public latencyMs = 500

    list(): Observable<Completion> {
        let completion: Completion = new Completion([
            this.createResource(),
            this.createResource(),
            this.createResource(),
        ]);
        return Observable.of(completion).delay(this.latencyMs);
    }

    createResource(): Resource {
        let n = Math.ceil(Math.random() * 100)
        return new Resource('Entry ' + n, 'entry_' +  + n)
    }
}
