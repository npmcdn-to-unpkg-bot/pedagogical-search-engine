var fs = require('fs');
var wp = require('webpage');

// Includes
// Generic wait-for function
function waitFor(testFn, successFn, timeOutFn, rejectedFn, finallyFn, timeOutMs, refreshMs) {
    var start = new Date().getTime(),
        condition = 0, // 0: false, 1: true, 2: definitively false
        interval = setInterval(function() {
    	var elapsed = new Date().getTime() - start;
        if((elapsed < timeOutMs) && (condition == 0)) {
            condition = testFn(elapsed);
        } else {
            if(condition == 0) {
            	// Timeout Case
                timeOutFn(elapsed, finallyFn);
                clearInterval(interval);
            } else if(condition == 1) {
            	// Success Case
                successFn(finallyFn);
                clearInterval(interval);
            } else {
            	// Reject Case
                rejectedFn(elapsed, finallyFn, condition);
                clearInterval(interval);
            }
        }
    }, refreshMs);
};

// Handling in/out put
var articlesFolder = 'output/articles';
var pagesFolder = articlesFolder + '/pages';
var screenshotsFolder = articlesFolder + '/screenshots';
var debuggingFolder = articlesFolder + '/debugging';
var dataPath = articlesFolder + '/data.json';
var data = JSON.parse(fs.read(dataPath));

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
//* Uncomment for log messages in .evaluate sections
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

function saveForDebugging(name) {
	// Take a screenshot [for debugging]
	var screenshotPath = debuggingFolder + '/' + name + '.png';
	page.render(screenshotPath, {
    	format: 'png',
    	quality: '25'
    });
	
	// Save page
	var content = page.content;
	var pagePath = debuggingFolder + '/' + name + '.html';
	fs.write(pagePath, content, 'w');
}

// Open each domain link
var timeoutMs = 20 * 1000;
var refreshMs = 1 * 500;
var scholarpediaDomain = 'http://www.scholarpedia.org';

function openLinks(position) {
	// Define iterator.next
	function goNextIfPossible() {
        if((position + 1) < data.length) {
        	openLinks(position + 1);
        } else {
        	closeAndExit(page);
        }
	}

	var article = data[position];
	var currentUrl = scholarpediaDomain + article.href;
	var uniqueName = position + '';

	// Was the page already processed?
	if(article.hasOwnProperty('status')) {
		var status = article.status;
		console.log('Passing(status: ' + status + ') ' + currentUrl);
		goNextIfPossible();
	} else {
		console.log('Loading ' + currentUrl);

		page.open(currentUrl, function(status) {
			// Check for page load success
			if(status !== 'success') {
				console.log("Unable to access network, status: " + status + ', url: ' + currentUrl);
			} else {
				page.includeJs('http://ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js', function() {
					var curatorSel = '.cp-curator-box';
					var mathLoadSel = '#MathJax_Message';
					var redirectSel = '#contentSub .mw-redirect';

			        // Wait for page-load
			        var undefinedValue = 'undefined-value';
					article.status = undefinedValue; // we want article.status to be "pass by reference"
			        waitFor(
			        	function(elapsed) {
			        		console.log(elapsed);
			        		// Test if the page has loaded
				            return page.evaluate(function(elapsed, curatorSel, mathLoadSel, redirectSel) {
				            	// Only take currated articles
				            	var isCurrated = ($(curatorSel).length > 0);
				            	if(isCurrated) {
				            		// Only take source articles
				            		var hasBeenRedirected = ($(redirectSel).length > 0);
				            		if(!hasBeenRedirected) {
					            		console.log('source.: YES');

					            		// Wait until it is fully loaded
						            	var mathLoad = $(mathLoadSel);
						            	console.log('curator: YES');

						            	if(mathLoad.length > 0) {
						            		var finishLoading = (mathLoad.css('display') === 'none');
						            		console.log('mathJax: ' + mathLoad.css('display'));

						            		if(finishLoading) {
						            			return 1; // success
						            		} else {
						            			return 0; // retry
						            		}
						            	} else {
						            		return 1; // success
						            	}
				            		} else {
					            		console.log('source.: NO');
				            			return 3; // reject definitively
				            		}
				            	} else {
					            	console.log('curator: NO');
					            	if(elapsed > 3000) {
					            		return 2; // reject definitively
					            	} else {
				            			return 0; // retry
					            	}
				            	}
				            }, elapsed, curatorSel, mathLoadSel, redirectSel);

				        }, function(finallyFn) {
				        	// When we think that the page has loaded
				        	// ..
				        	console.log('> OK');

							// Take a screenshot
							var screenshotPath = screenshotsFolder + '/' + uniqueName + '.png';
							page.render(screenshotPath, {
				            	format: 'png',
				            	quality: '25'
					        });

					        // Save the page
							var content = page.content;
							var localPath = pagesFolder + '/' + uniqueName + '.html';
							fs.write(localPath, content, 'w');
							article.page = localPath;

							// Signal that the work was done
							article.status = 'downloaded';

							// Continue further the work
				        	finallyFn();

				        }, function(elapsed, finallyFn) {
				        	// Page timeout
				        	var cStatus = 'timeout';
							console.log('> ' + cStatus);
							article.status = cStatus;
							saveForDebugging(cStatus);

				            // Continue
				            finallyFn();

				        }, function(elapsed, finallyFn, rejectNb) {
				        	// Page rejected
				        	if(rejectNb == 2) {
				        		article.status = 'no-curator';
				        	} else {
				        		article.status = 'redirect';
				        	}
				        	var cStatus = article.status;
							console.log('> ' + cStatus);
							saveForDebugging(cStatus);

				            // Continue
				            finallyFn();

				        }, function() {
				        	// Write the work
				        	if(article.status === undefinedValue) {
				        		article.status = undefined; // javascript-hook
				        	}
				        	fs.write(dataPath, JSON.stringify(data, null, 3), 'w');

				            // Continue the work
				            goNextIfPossible();
						}, timeoutMs, refreshMs
				    );
	        	});
			}
		});
	}
}

openLinks(0);
