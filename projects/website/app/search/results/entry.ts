import {Snippet} from "./snippet";

export class Entry {
    constructor(
        public title: String = '',
        public typeText: String = '',
        public href: String = '',
        public snippet: Snippet = new Snippet(),
        public score: number
    ){}
}