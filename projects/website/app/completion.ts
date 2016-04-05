import {Proposition} from './proposition'

export class Completion {
    constructor(public propositions: Array<Proposition>) {}
    
    reset() {
        this.propositions = []
    }

    update(other: Completion) {
        delete this.propositions
        this.propositions = other.propositions
    }
}
