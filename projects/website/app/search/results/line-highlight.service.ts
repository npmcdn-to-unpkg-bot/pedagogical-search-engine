import {Injectable} from "angular2/core";
import {HighlightService} from "../../utils/highlight.service";
import {Line} from "./line";
import {Spot} from "./spot";

@Injectable()
export class LineHighlightService {
    constructor(private _hs: HighlightService) {}

    private _cache = {};

    // Public methods
    public getHightlight(uri: String): String {
        let formatted = uri.toLowerCase().trim();

        // Check whether we have assigned a highlight to this uri
        if(formatted in this._cache) {
            // we did
            return this._cache[formatted];
        } else {
            // Get a new highlight
            let nh = this._hs.getHightlight(Object.keys(this._cache).length);
            this._cache[formatted] = nh;
            return nh;
        }
    }

    public highlight(line: Line): String {
        // Order Desc
        let ordered: Array<Spot> = line.spots.sort((s1: Spot, s2: Spot) => {
            return s2.start - s1.start;
        });

        // Handle overlapping spots
        let handled: Array<Spot> = [];
        for(let spot1 of ordered) {
            if(handled.length === 0) {
                handled.push(spot1);
            } else {
                let spot2 = handled[handled.length - 1];
                let stop = Math.min(spot2.start, spot1.stop);
                if(stop !== spot1.start) {
                    handled.push(new Spot(spot1.start, stop, spot1.uri));
                }
            }
        }

        // Add annotations
        let text = line.text;
        for(let spot of handled) {
            let t1 = text.substring(spot.start, spot.stop);
            let t2 = this._hs.encloseWithSpan(t1, this.getHightlight(spot.uri));

            let p1 = text.substring(0, spot.start);
            let p2 = text.substring(spot.stop);

            text = p1 + t2 + p2;
        }

        return text;
    }
}