
export class SearchTerm {
    constructor(public label: String, public uri: String) {}

    // Construct a request object from some search terms
    // This one is commonly requested by web services
    static wsRepresentation(searchTerms: Array<SearchTerm>)
    : Array<any> {
        let request: Array<any> = [];
        for(let searchTerm of searchTerms) {
            if(searchTerm.uri.length > 0) {
                request.push({
                    "label": searchTerm.label,
                    "uri": searchTerm.uri
                });
            } else {
                request.push({
                    "label": searchTerm.label
                });
            }
        }

        return request;
    }
}