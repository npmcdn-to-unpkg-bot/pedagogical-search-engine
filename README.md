
TL;DR
------------
This search engine suggests online courses (Coursera/KhanAcademy/MIT) & books based on Wikipedia topics.

It can search at the finest granularity level (Sections, Chapters, Modules).

An online demo is available at: [wikichimp.com](https://www.wikichimp.com)

The algorithm in a nutshell
------------
Each document is transformed into a set of topics.
These topics appear in the document but are not necessarily central.

A graph analysis is performed to filter the most relevant topics which are later indexed in the search engine.

A few links
------------

* [Project report] [1]
* [The search engine indexer code] [2]
* [The website code code] [3]

[1]:https://github.com/pacm/pedagogical-search-engine/blob/master/report.pdf
[2]:https://github.com/pacm/pedagogical-search-engine/tree/master/projects/scala
[3]:https://github.com/pacm/pedagogical-search-engine/tree/master/projects/website
