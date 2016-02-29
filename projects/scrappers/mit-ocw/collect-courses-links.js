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

function doesMatch(id, href, replaceAll) {
	var formattedId = replaceAll(id, '\\.', '-').toLowerCase() + '-';
	var chunks = href.split('/');
	var resourceName = chunks[chunks.length - 1];
	return (resourceName.indexOf(formattedId) == 0);
};

function beautify(str, replaceAll) {
	var r1 = replaceAll(str, '[^\\S ]', '');
	var r2 = replaceAll(r1, '[ ]{2,}', ' ');
	return r2.trim();
};

function getUniqueName(url) {
	var chunks = url.split('/');
	var chunks2 = [];
	for(var i = 0; i < chunks.length; i++) {
		var c = chunks[i];
		if(c.trim() !== '') {
			chunks2.push(c);
		}
	}
	return chunks2[chunks2.length - 1].toLowerCase();
};

function deducecourseNumber(url, name) {
	var chunks = name.split(' ');
	var chunks2 = [];
	for(var i = 0; i < chunks.length; i++) {
		var c = chunks[i];
		if(c.trim() !== '') {
			chunks2.push(c);
		}
	}
	if(chunks2.length > 0) {
		var word1 = chunks2[0].toLowerCase().replace(new RegExp('[^\\S]'), '');
		var chunks3 = url.split('/');
		var endUrl = chunks3[chunks3.length - 1];
		var index = endUrl.indexOf('-' + word1);
		var courseNumber = endUrl.substr(0, index);
		return courseNumber;
	} else {
		console.log('Cannot deduce main Id for url: ' + url);
		return 'PROBLEM_WITH_DEDUCE_MAIN_ID_IN_SCRAPPER';
	}
};

// Handling in/out put
var courseFolder = 'output/courses';
var debuggingFolder = courseFolder + '/debugging';
var dataPath = courseFolder + '/data.json';
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

// Open each domain link
var timeoutMs = 20 * 1000;
var refreshMs = 1 * 500;
var mitocwDomain = 'http://ocw.mit.edu';

var url = mitocwDomain + '/courses/';
console.log('Loading ' + url);
page.open(url, function(status) {
	// Check for page load success
	if(status !== 'success') {
		console.log("Unable to access network, status: " + status + ', url: ' + url);

		closeAndExit();
	} else {
		var courseLineSel = '#course_wrapper .courseList tbody tr';

        // Wait for page-load
        waitFor(
        	function(elapsed) {
        		// Test if the page has loaded
	            return page.evaluate(function(courseLineSel) {
	            	// Has the page loaded?
	            	var courses = $(courseLineSel);

	            	if(courses.length > 2500) {
	            		return 1;
	            	} else {
	            		return 0;
	            	}
	            }, courseLineSel);

	        }, function(finallyFn) {
	        	// When we think that the page has loaded
	        	// ..
	        	console.log('Page loaded');

		        // Collect the links
		        var links = page.evaluate(function(sel, beautify, doesMatch, replaceAll, deducecourseNumber,
		        		getUniqueName) {
					return $.map($(sel), function(e) {
						var colLinks = $(e).find("a");
						var courseNumber = beautify($(colLinks[0]).text(), replaceAll).toLowerCase();
						// Name will be scrapped in the next steps
						var level = beautify($(colLinks[2]).text(), replaceAll).toLowerCase();
						var href = $(colLinks[1]).attr('href');

						// Handle "Supplemental" courses
						if(courseNumber === 'supplemental') {
							var name = beautify($(colLinks[1]).text(), replaceAll);
							courseNumber = deducecourseNumber(href, name);
						}
						
						// Remove redirect-links
						// e.g. "Management in Engineering" has id "16.653" but links to course "2.96"
						if(doesMatch(courseNumber, href, replaceAll)) {
							if(level === 'graduate' || level === 'undergraduate') {
								return {
									courseNumber: courseNumber,
									uniqueName: getUniqueName(href),
									level: level,
									href: href
								};
							} else {
								console.log('Unrecognized level: ' + level);
							}
						}
					});
				}, courseLineSel, beautify, doesMatch, replaceAll, deducecourseNumber, getUniqueName);

		        // Save the links
		        data = links;
		        fs.write(dataPath, JSON.stringify(data, null, 3), 'w');

				// Continue further the work
	        	finallyFn();

	        }, function(elapsed, finallyFn) {
	        	// Page timeout
				console.log('Page timeout');
				saveForDebugging('timeout');

	            // Continue
	            finallyFn();

	        }, function(elapsed, finallyFn, rejectNb) {
	        	// Page rejected
				console.log('Page rejected');
				saveForDebugging('rejected');

	            // Continue
	            finallyFn();

	        }, function() {
	        	closeAndExit();

			}, timeoutMs, refreshMs
	    );
	}
});

// Workflow handling
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

  closeAndExit();
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

  closeAndExit();
};

})();
