import {Injectable} from "angular2/core";
import {Http} from "angular2/http";
import {Observable} from "rxjs/Rx";
import {Completion} from "./completion";
import {CompletionService} from "./completion.service";
import {Result} from "./result/result";
import {Entity} from "./result/entity";

@Injectable()
export class MockCompletionService extends CompletionService {
    constructor(private _http: Http) {}

    public latencyMs = 500

    list(text: String): Observable<Completion> {
        let completion: Completion = new Completion([
            this.createResource("Umbrella (song)"),
            this.createResource("Umbrella"),
            this.createResource("Umbrella Movement"),
        ]);
        return Observable.of(completion).delay(this.latencyMs);
    }

    createResource(pre: String = "Entry"): Result {
        let n = Math.ceil(Math.random() * 100);
        return new Entity(`${pre} ${n}`, `${pre} ${n}`)
    }
}
