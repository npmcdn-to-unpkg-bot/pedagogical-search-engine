var fs = require('fs');
var wp = require('webpage');

// Includes
// Generic wait-for function
function waitFor(testFn, successFn, timeOutFn, finallyFn, timeOutMs, refreshMs) {
    var start = new Date().getTime(),
        condition = false,
        interval = setInterval(function() {
    	var elapsed = new Date().getTime() - start;
        if((elapsed < timeOutMs) && !condition) {
            condition = testFn(elapsed);
        } else {
            if(!condition) {
                timeOutFn(elapsed, finallyFn);
                clearInterval(interval);
            } else {
                successFn(finallyFn);
                clearInterval(interval);
            }
        }
    }, refreshMs);
};

// Handling in/out put
var articlesFolder = 'output/articles';
var pagesFolder = articlesFolder + 'pages';
var screenshotsFolder = articlesFolder + '/screenshots';
var dataPath = articlesFolder + '/data.json';
var data = [];

// Collecting Job
// ..
// Create the page
var page = wp.create();
page.settings.userAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:44.0) Gecko/20100101 Firefox/44.0";
var width = 1280;
var height = 1024;
page.viewportSize = {
  width: width,
  height: height
};
/* Uncomment for log messages in .evaluate sections
page.onConsoleMessage = function(msg) {
  console.log(msg);
};
// */

function closeAndExit(page) {
	// Finally, quitting
	console.log('Quitting Script');
	page.close();
	phantom.exit();
}

// Open each domain link
var timeoutMs = 10 * 1000;
var refreshMs = 1 * 1000;
var scholarpediaDomain = 'http://www.scholarpedia.org';
var listLinks = [
	'/w/index.php?title=Special:AllPages&from=%2FAPPENDIX',
	'/w/index.php?title=Special:AllPages&from=Boris+Chirikov',
	'/w/index.php?title=Special:AllPages&from=Degasperis-Procesi+equation',
	'/w/index.php?title=Special:AllPages&from=Functional+magnetic+resonance+imaging',
	'/w/index.php?title=Special:AllPages&from=In+vivo+intracellular+recording',
	'/w/index.php?title=Special:AllPages&from=Models+of+dopaminergic+modulation',
	'/w/index.php?title=Special:AllPages&from=Partial+differential+equation%2FSecond-Order+Partial+Differential+Equations',
	'/w/index.php?title=Special:AllPages&from=Sarkovskii+theorem',
	'/w/index.php?title=Special:AllPages&from=Systems+Neuroscience+of+Touch'
];

function openList(links, position) {
	var currentUrl = scholarpediaDomain + links[position];
	console.log('Loading ' + currentUrl);

	page.open(currentUrl, function(status) {
		// Check for page load success
		if(status !== 'success') {
			console.log("Unable to access network, status: " + status + ', url: ' + currentUrl);
		} else {
			page.includeJs('http://ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js', function() {
				var linkSel = '.mw-allpages-table-chunk td a';

		        // Wait for page-load
		        waitFor(
		        	function(elapsed) {
		        		// Test if the page has loaded
			            return page.evaluate(function(linkSel) {
			            	var links = $(linkSel);
			            	return (links.length > 10);
			            }, linkSel);

			        }, function(finallyFn) {
			        	// When we think that the page has loaded
			        	// ..
			        	// wait an additional time
			        	setTimeout(function() {
			        		// Get the links
					        var articleLinks = page.evaluate(function(sel, data) {
								return $.map($(sel), function(e) {
									return {
										href: $(e).attr('href'),
										label: $(e).text()
									};
								});
							}, linkSel, data);

				        	// Save the links
				        	for(var i = 0; i < articleLinks.length; i++) {
				        		data.push(articleLinks[i]);
				        	}
							fs.write(dataPath, JSON.stringify(data, null, 3), 'w');
							console.log(articleLinks.length + ' links discovered!');

				            // Continue
				            finallyFn();
			        	}, 1 * 1000);
			        }, function(elapsed, finallyFn) {
			        	// If the page cannot be loaded, pass it
						console.log('Skipping course ' + uniqueName);

			            // Continue
			            finallyFn();
			        }, function() {
			            // Continue the work
			            if((position + 1) < links.length) {
			            	openList(links, position + 1);
			            } else {
			            	closeAndExit(page);
			            }
					}, timeoutMs, refreshMs
			    );
		    });
		}
	});
}

openList(listLinks, 0);
