import {Entry} from "./entry";

export class Response {
    constructor(public entries: Array<Entry>,
                public nbResults: number){}
}