import {Injectable} from "angular2/core";
import {WordsService} from "./words.service";

@Injectable()
export class HighlightService {
    constructor(private _wordsService: WordsService) {}

    private _classPrefix: String = "wc-com-highlight-";
    private _highlights: Array<String> = [
        "t1",
        "t2",
        "t3",
        "t4",
        "t5",
        "t6",
        "t7",
        "t8"
    ];

    // Public methods
    public getHightlight(n: number): String {
        return this._highlights[n % this._highlights.length];
    }

    public encloseWithSpan(text: String, highlight: String): String {
        let open = `<span class="${this._classPrefix}${highlight}">`;
        let close = `</span>`;
        return `${open}${text}${close}`;
    }

    public sampleText(): String {
        let t = "";
        for(let highlight of this._highlights) {
            t += this._wordsService.genPhraseBetween(3, 15);
            t += this.encloseWithSpan(this._wordsService.genPhraseBetween(1, 4), highlight);
        }
        t += this._wordsService.genPhraseBetween(3, 5);

        return t;
    }

}