(function() {

var fs = require('fs');
var wp = require('webpage');

// Includes
// Generic wait-for function
function waitFor(testFn, successFn, timeOutFn, rejectedFn, finallyFn, timeOutMs, refreshMs) {
    var start = new Date().getTime(),
        condition = 0, // 0: false, 1: true, 2, ..: definitively false
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

// ReplaceAll
// source: http://stackoverflow.com/questions/1144783/replacing-all-occurrences-of-a-string-in-javascript
function replaceAll(str, search, replacement) {
    return str.replace(new RegExp(search, 'g'), replacement);
};

function normalize(str, replaceAll) {
	var r1 = replaceAll(str, '[^\\S ]', ' ');
	var r2 = replaceAll(r1, '[ ]{2,}', ' ');
	return r2.trim().toLowerCase();
};

// Handling in/out put
var courseFolder = 'output/courses';
var debuggingFolder = courseFolder + '/debugging';
var screenshotsFolder = courseFolder + '/screenshots';
var homeFolder = courseFolder + '/home';
var dataPath = courseFolder + '/data.json';
var finishedPath = 'finished.info';
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
var mitocwDomain = 'http://ocw.mit.edu';

var completeSuccess = true;
function openLinks(position) {
	// Define iterator.next
	function goNext() {	
        if((position + 1) < data.length) {
        	openLinks(position + 1);
        } else {
        	console.log('The script has finished ..');

        	// Signal success
        	if(completeSuccess) {
        		console.log('Complete success');
        		signalSuccess();
        	}

        	// Quit
        	closeAndExit();
        }
	}

	var course = data[position];
	var courseName = course.uniqueName;
	var currentUrl = mitocwDomain + course.href;
	var expandedStatus = 'expanded';

	// Was the page already processed?
	if(course.hasOwnProperty('status') && course.status === expandedStatus) {
		console.log('Passing(status: ' + expandedStatus + ') ' + courseName);
		goNext();
	} else {
		console.log('Loading: ' + courseName);

		page.open(currentUrl, function(status) {
			// Check for page load success
			if(status !== 'success') {
				console.log("Unable to access network, status: " + status + ', url: ' + currentUrl);
				completeSuccess = false;

	        	// Continue
				goNext();
			} else {
				page.includeJs('http://ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js', function() {
					var titleSel = '#course_title';
					var menuLinksSel = '#course_nav a';

			        // Wait for page-load
			        waitFor(
			        	function(elapsed) {
			        		console.log(elapsed);

			        		// Test if the page has loaded
				            return page.evaluate(function(elapsed, titleSel) {
				            	// Check if the page looks OK
				            	var title = $(titleSel).text();

				            	if(title.length > 4) {
				            		return 1;
				            	} else {
				            		return 0;
				            	}
				            }, elapsed, titleSel);

				        }, function(finallyFn) {
				        	// Page has successfully loaded
				        	// ..
				        	console.log('> OK');

				        	// Create the pages section
				        	course.pages = [];

							// Get the other links
							var menuLinks = page.evaluate(function(menuLinksSel, normalize, replaceAll) {
								var menuLinks = $(menuLinksSel);
								return $.map(menuLinks, function(e) {
									var href = $(e).attr('href');
									var label = normalize($(e).text(), replaceAll);
									// (a) non-zero labels (b) local links
									if(label !== '' && href.indexOf('http') === -1) {
										return {
											normalizedLabel: label,
											href: href
										};
									}
								});
							}, menuLinksSel, normalize, replaceAll);
							
							// Save the other links
							for(var i = 0; i < menuLinks.length; i++) {
								course.pages.push(menuLinks[i]);
								console.log('> "' + menuLinks[i].normalizedLabel + '"');
							}

							// The course was expanded
							course.status = expandedStatus;

							// Continue further the work
				        	finallyFn();

				        }, function(elapsed, finallyFn) {
				        	// Page timeout
							console.log('> timeout');
							saveForDebugging('timeout-' + courseName);
							completeSuccess = false;

				            // Continue
				            finallyFn();

				        }, function(elapsed, finallyFn, rejectNb) {
				        	// Page rejected
				        	saveForDebugging(cStatus + '/' + course.label);
							completeSuccess = false;

				            // Continue
				            finallyFn();

				        }, function() {
				        	// Write the work
				        	fs.write(dataPath, JSON.stringify(data, null, 3), 'w');

				            // Continue the work
				            goNext();
						}, timeoutMs, refreshMs
				    );
	        	});
			}
		});
	}
}

// Workflow handling
function closeAndExit() {
	// Finally, quitting
	console.log('Quitting Script');
	page.close();
	phantom.exit();
};

function signalSuccess() {
	console.log('create ' + finishedPath);
	fs.write(finishedPath, 'say yes', 'w');
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

// Initial start
openLinks(0);

})();
