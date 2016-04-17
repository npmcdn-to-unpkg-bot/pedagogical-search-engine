import {Injectable} from "angular2/core";

@Injectable()
export class AngularFixService {
    public encodeURIComponent(str: String): String {
        return encodeURIComponent(str).replace(/[!'()*]/g, function (c) {
            return '%' + c.charCodeAt(0).toString(16);
        });
    }
}