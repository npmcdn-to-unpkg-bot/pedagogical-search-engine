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

var dontTerminate = false;
function openLinks(position) {
	// Define iterator.next
	function goNext() {
        if((position + 1) < data.length) {
        	openLinks(position + 1);
        } else {
        	console.log('The script has finished ..');
        	if(dontTerminate) {
        		console.log('.. and was asked not to terminate');
        		console.log('.. hence restarting..');
        		restart();
        	} else {
        		closeAndExit();
        	}
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
				dontTerminate = true;

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
									if(label !== '') {
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
							dontTerminate = true;

				            // Continue
				            finallyFn();

				        }, function(elapsed, finallyFn, rejectNb) {
				        	// Page rejected
				        	saveForDebugging(cStatus + '/' + course.label);
							dontTerminate = true;

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
function restart() {
	openLinks(0);
};

function closeAndExit() {
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

})();
