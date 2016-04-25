import {Snippet} from "./snippet";
import {Quality} from "./quality";

export class Entry {
    constructor(
        public title: String = '',
        public typeText: String = '',
        public href: String = '',
        public snippet: Snippet = new Snippet(),
        public quality: Quality,
        public rank: number
    ){}

    // Public
    public isHighQuality(): boolean {
        return this.quality === Quality.high;
    }
    public isLowQuality(): boolean {
        return this.quality === Quality.low;
    }
}