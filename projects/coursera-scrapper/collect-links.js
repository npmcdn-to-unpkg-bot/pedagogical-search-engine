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
                timeOutFn(elapsed);
                finallyFn();
            } else {
                successFn();
                finallyFn();
                clearInterval(interval);
            }
        }
    }, refreshMs);
};

// Handling output
var outputPath = 'output/courses-links.txt';
console.log('Opening ' + outputPath);
var output = fs.open('courses-links.txt', {
	mode: 'w',
	charset: 'UTF-8'
});

// Collecting Job
// ..
// Create the page
var page = wp.create();
page.settings.userAgent = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:44.0) Gecko/20100101 Firefox/44.0";
page.viewportSize = {
  width: 1280,
  height: 1024
};

// Open catalog
var timeoutMs = 60 * 60  * 1000; // 1-hour
var refreshMs = 1 * 1000; // 1 sec
page.open("http://www.coursera.org/browse?languages=en", function (status) {
    // Check for page load success
    if (status !== "success") {
        console.log("Unable to access network");
    } else {
        // Wait for page-load
        waitFor(
        	function(elapsed) {
        		// Test if the page has loaded
	            return page.evaluate(function() {
	            	var links = $("div.rc-DomainNav a");
	            	console.log("booh " + links.length);
	                return links.length == 10;
	            });
	        }, function(elapsed) {
	        	// When the page has loaded
	        	page.render('example.png');
	            console.log("success");
	        }, function(elapsed) {
	        	// If the page cannot be loaded
	        	console.error("Cannot load the catalog-page.");
	        }, function() {
				// Finally, quitting
				console.log('Quitting Script');
				output.close();
				phantom.exit();
			}, timeoutMs, refreshMs
	    );        
    }
});