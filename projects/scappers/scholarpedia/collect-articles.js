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
		console.log('Passing(status: ' + status + ') ' + article.href);
		goNextIfPossible();
	} else {
		console.log('Loading ' + article.href);

		page.open(currentUrl, function(status) {
			// Check for page load success
			if(status !== 'success') {
				console.log("Unable to access network, status: " + status + ', url: ' + currentUrl);
			} else {
				page.includeJs('http://ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js', function() {
					var logoSel = '#p-logo';
					var loginSel = '#pt-login';
					var searchSel = '#simpleSearch';
					var contentSel = '#content';
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
				            return page.evaluate(function(elapsed, curatorSel, mathLoadSel, redirectSel,
				            	logoSel, loginSel, searchSel, contentSel) {
				            	// Check if the page looks OK
				            	var logo = $(logoSel);
				            	var login = $(loginSel);
				            	var search = $(searchSel);
				            	var contentEl = $(contentSel);
				            	var looksOK = (logo.length > 0) && (login.length > 0)
				            		&& (search.length > 0) && (contentEl.length > 0);

				            	var shouldHaveLoadedTimeout = 3000;
				            	if(looksOK) {
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
						            	if(elapsed > shouldHaveLoadedTimeout) {
						            		return 2; // reject definitively
						            	} else {
					            			return 0; // retry
						            	}
					            	}
				            	} else {
					            	console.log('looksOK: NO');
					            	if(elapsed > shouldHaveLoadedTimeout) {
					            		return 4; // reject definitively
					            	} else {
				            			return 0; // retry
					            	}
				            	}
				            }, elapsed, curatorSel, mathLoadSel, redirectSel, logoSel, loginSel, searchSel, contentSel);

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
							//saveForDebugging(cStatus + '/' + article.label);

				            // Continue
				            finallyFn();

				        }, function(elapsed, finallyFn, rejectNb) {
				        	// Page rejected
				        	if(rejectNb == 2) {
				        		article.status = 'no-curator';
				        		var cStatus = article.status;
				        	} else if(rejectNb == 3) {
				        		article.status = 'redirect';
				        		cStatus = article.status;
				        	} else {
				        		// We should retry later
				        		var cStatus = 'retry-later';
				        	}
							console.log('> ' + cStatus);
							//saveForDebugging(cStatus + '/' + article.label);

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

// Workflow handling
function restart() {
	openLinks(0);
};

function closeAndExit(page) {
	// Finally, quitting
	console.log('Quitting Script');
	page.close();
	phantom.exit();
};

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
};

// Error handling
// ..
//* Uncomment for log messages in .evaluate sections
page.onConsoleMessage = function(msg) {
  console.log(msg);
};
// */

// .. fatal error
phantom.onError = function(msg, trace) {
  var msgStack = ['phantom error: ' + msg];
  if (trace && trace.length) {
    msgStack.push('TRACE:');
    trace.forEach(function(t) {
      msgStack.push(' -> ' + (t.file || t.sourceURL) + ': ' + t.line + (t.function ? ' (in function ' + t.function +')' : ''));
    });
  }
  console.error(msgStack.join('\n'));
  
  // Restart on error
  console.log('>>            <<');
  console.log('>> RESTARTING <<');
  console.log('>>            <<');
  restart();
};

// .. page error
page.onError = function(msg, trace) {

  var msgStack = ['Page error: ' + msg];

  if (trace && trace.length) {
    msgStack.push('TRACE:');
    trace.forEach(function(t) {
      msgStack.push(' -> ' + t.file + ': ' + t.line + (t.function ? ' (in function "' + t.function +'")' : ''));
    });
  }

  console.error(msgStack.join('\n'));
  
  // Restart on error
  console.log('>>            <<');
  console.log('>> RESTARTING <<');
  console.log('>>            <<');
  restart();
};

// Initial start
restart();