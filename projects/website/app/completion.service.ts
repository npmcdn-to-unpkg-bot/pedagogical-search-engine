import {Injectable} from 'angular2/core'
import {Http} from 'angular2/http'
import {Observable} from "rxjs/Rx";
import {Completion} from './completion'
import {Proposition} from './proposition'

@Injectable()
export class CompletionService {
    constructor(private _http: Http) {

    }

    list(): Observable<Completion> {
        let n1: number = this.rand()
        let n2: number = this.rand()
        let n3: number = this.rand()
        return Observable.of(new Completion([
            new Proposition('entry ' + n1, 'entry_' +  + n1),
            new Proposition('entry ' + n2, 'entry_' +  + n2),
            new Proposition('entry ' + n3, 'entry_' +  + n3),
        ])).delay(500);
    }

    rand(): number {
        return Math.ceil(Math.random() * 100)
    }
}
