import {Resource} from "./resource";

export class Completion {
    constructor(public propositions: Array<Resource>) {}
    
    reset() {
        this.propositions = []
    }

    update(other: Completion) {
        delete this.propositions
        this.propositions = other.propositions
    }
}
