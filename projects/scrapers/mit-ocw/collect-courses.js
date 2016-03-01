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

// Handling in/out put
var debuggingFolder = 'output/debugging';
var screenshotsFolder = 'output/screenshots';
var coursesFolder = 'output/courses'
var dataPath = 'output/data.json';
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
function openLinks(courseNo, linkNo) {
	// Define iterator.next
	function goNext() {
		var course = data[courseNo];
		if((linkNo + 1) < course.pages.length) {
			openLinks(courseNo, linkNo + 1);
		} else {
			if((courseNo + 1) < data.length) {
				openLinks(courseNo + 1, 0);
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
	}

	var course = data[courseNo];
	var coursePage = course.pages[linkNo];
	var currentLabel = course.courseNumber + ' > ' + coursePage.normalizedLabel;
	var currentUrl = mitocwDomain + coursePage.href;
	var doneStatus = 'downloaded';

	// Was the page already processed?
	if(coursePage.hasOwnProperty('status') && coursePage.status === doneStatus) {
		console.log('Passing(status: ' + coursePage.status + ') ' + currentLabel);
		goNext();
	} else {
		console.log('Loading: ' + currentLabel);

		page.open(currentUrl, function(status) {
			// Check for page load success
			if(status !== 'success') {
				console.log("Unable to access network, status: " + status + ', url: ' + currentUrl);
				completeSuccess = false;

	        	// Continue
				goNext();
			} else {
				page.includeJs('http://ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js', function() {
					var menuSel = '#course_nav li';

			        // Wait for page-load
			        waitFor(
			        	function(elapsed) {
			        		console.log(elapsed);

			        		// Test if the page has loaded
				            return page.evaluate(function(elapsed, menuSel) {
				            	// Check if the page looks OK
				            	var menuLinks = $(menuSel);

				            	if(menuLinks.length > 0) {
				            		return 1;
				            	} else {
				            		return 0;
				            	}
				            }, elapsed, menuSel);

				        }, function(finallyFn) {
				        	// Page has successfully loaded
				        	// ..
				        	console.log('> OK');

							// Download the page
							var content = page.content;
							var localPath = coursesFolder + '/' +
								course.uniqueName + '/' + coursePage.normalizedLabel + '.html';
							fs.write(localPath, content, 'w');

							// Save the local Path
							coursePage.localPath = localPath;

							// The course was expanded
							coursePage.status = doneStatus;

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
openLinks(0, 0);

})();
