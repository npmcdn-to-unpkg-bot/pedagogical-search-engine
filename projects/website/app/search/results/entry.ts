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
    public hasHref(): boolean {
        return (this.href.length > 0);
    }
    public isBook(): boolean {
        return (this.typeText.toLowerCase() === "book");
    }
    public googleHref(): String {
        let query = encodeURI(this.title);

        return `https://google.com/search?q=${query}`;
    }
}