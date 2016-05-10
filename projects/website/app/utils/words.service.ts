import {Injectable} from "angular2/core";

@Injectable()
export class WordsService {
    constructor() {}

    private _words: Array<String> = [
        "the",
        "hello",
        "world",
        "a",
        "great",
        "amazing",
        "some",
        "cool",
        "people",
        "most",
        "countries",
        "united",
        "for",
        "genius",
        "persons",
        "folks",
        "team",
        "of"
    ];

    // Public methods
    public genPhrase(n: number): String {
        let phrase = "";
        for(let i = 0; i < n; i++) {
            let j = Math.floor(Math.random() * this._words.length);
            phrase += " " + this._words[j];
        }
        return phrase;
    }

    public genPhraseBetween(from: number, to: number): String {
        let gap = to - from + 1;
        let diff = Math.floor(Math.random() * gap);
        return this.genPhrase(from + diff);
    }
}