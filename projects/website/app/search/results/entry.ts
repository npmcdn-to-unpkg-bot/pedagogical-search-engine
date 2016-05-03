import {Snippet} from "./snippet";
import {Quality} from "./quality";
import {Classification} from "./classification";

export class Entry {
    constructor(
        public entryId: String = '',
        public title: String = '',
        public typeText: String = '',
        public href: String = '',
        public snippet: Snippet = new Snippet(),
        public quality: Quality,
        public rank: number
    ){}

    private _titleQuery = encodeURI(this.title);

    // Public
    public isHighQuality(): boolean {
        return this.quality === Quality.high;
    }
    public isLowQuality(): boolean {
        return this.quality === Quality.low;
    }
    public hasHref(): boolean {
        return this.href &&  (this.href.length > 0);
    }
    public isBook(): boolean {
        return (this.typeText.toLowerCase() === "book");
    }
    public googleHref(): String {

        return `https://google.com/search?q=${this._titleQuery}`;
    }
    public epflHref(): String {
        return `http://beast-epfl.hosted.exlibrisgroup.com/primo_library/libweb/action/search.do?fn=search&ct=search&initialSearch=true&mode=Basic&tab=default_tab&indx=1&dum=true&srt=rank&vid=EPFL&frbg=&tb=t&scp.scps=scope%3A%28EPFL_SFX%29%2CPrimo2Primo_41EPFL&vl%28freeText0%29=${this._titleQuery}`;
    }
}