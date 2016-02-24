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

// ReplaceAll
// source: http://stackoverflow.com/questions/1144783/replacing-all-occurrences-of-a-string-in-javascript
function replaceAll(str, search, replacement) {
    return str.replace(new RegExp(search, 'g'), replacement);
};

// Handling in/out put
var courseLinksPath = 'output/course-links.txt';
var data = JSON.parse(fs.read(courseLinksPath));
var outputPath = 'output/courses';

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

// Open each domain link
var timeoutMs = 10 * 1000;
var refreshMs = 1 * 1000;
var courseraDomain = 'https://www.coursera.org';
var pathPrefix = 'output/courses';
var paths = {
	pages: pathPrefix + '/pages',
	screenshot: pathPrefix + '/screenshots',
};

function closeAndExit(page) {
	// Finally, quitting
	console.log('Quitting Script');
	page.close();
	phantom.exit();
}
function openLink(data, domainPosition, subdomainPosition, coursePosition) {
	// define go next function
	function goNext() {
		var goToNextDomain = true;
        if((coursePosition + 1) < list.courses.length) {
        	console.log('new course');
        	openLink(data, domainPosition, subdomainPosition, coursePosition + 1);
			goToNextDomain = false;
        } else {
            // Check if we are in a subdomain
            if(typeof subdomain != "undefined") {
    			// Check if there are other subdomains
    			if((subdomainPosition + 1) < domain.subdomain.length) {
        			console.log('new subdomain, first course');
    				openLink(data, domainPosition, subdomainPosition + 1, 0);
    				goToNextDomain = false;
    			}
            }
        }

        if(goToNextDomain) {
        	// Check if there is a next domain
        	if((domainPosition + 1) < data.length) {
        		console.log('new domain, (first subdomain), first course');
        		openLink(data, domainPosition + 1, 0, 0);
        	} else {
	            // Quit
            	closeAndExit(page);
        	}
        }
	}

	// Get current list
	var domain = data[domainPosition];
	if('subdomain' in domain) {
		var subdomain = data[domainPosition].subdomain[subdomainPosition];
		var list = subdomain;
		var uniqueName = domainPosition + '-' + subdomainPosition;
	} else {
		subdomain = undefined;
		list = domain;
		uniqueName = domainPosition;
	}
	var course = list.courses[coursePosition];
	uniqueName = uniqueName + '-' + coursePosition;
	if(course.hasOwnProperty('localPath') && course.hasOwnProperty('screenshot')) {
		console.log('Passing course ' + uniqueName);
		goNext();
	} else {
		var currentUrl = courseraDomain + course.href;
		page.open(currentUrl, function(status) {
			// Check for page load success
			if(status !== 'success') {
				console.log("Unable to access network, status: " + status + ', url: ' + currentUrl);
			} else {
				var aboutSel = '.about-container'; // Courses v1
				var aboutSel2 = '.c-cd-section'; // Courses v2
				var aboutSel3 = '.rc-AboutS12n'; // Specializations
				var aboutSel4 = '.rc-WhatYouLearnSection'; // Course v3 (mentor-guided?) (e.g. https://www.coursera.org/learn/project-management)
				var aboutSel5 = '.content'; // Program (e.g. https://www.coursera.org/course/imba)

		        // Wait for page-load
		        waitFor(
		        	function(elapsed) {
		        		// Test if the page has loaded
			            return page.evaluate(function(aboutSel, aboutSel2, aboutSel3, aboutSel4, aboutSel5) {
			            	var about = $(aboutSel);
			            	var about2 = $(aboutSel2);
			            	var about3 = $(aboutSel3);
			            	var about4 = $(aboutSel4);
			            	var about5 = $(aboutSel5);
			            	return (about.text().length > 0) || (about2.text().length > 0)
			            		|| (about3.text().length > 0) || (about4.text().length > 0)
			            		|| (about5.text().length > 0);
			            }, aboutSel, aboutSel2, aboutSel3, aboutSel4, aboutSel5);
			        }, function(finallyFn) {
			        	// When we think that the page has loaded
			        	// ..
			        	// wait an additional time
			        	setTimeout(function() {
				        	// Download the course
							var content = page.content;
							var localPath = paths.pages + '/' + uniqueName + '.html';
							fs.write(localPath, content, 'w');
							console.log('Course ' + localPath + ' written');

							// Take a screenshot
							var screenshotPath = paths.screenshot + '/' + uniqueName + '.png';
							page.render(screenshotPath, {
				            	format: 'png',
				            	quality: '100'
					        });

							// Save the path to the course file
							course.localPath = localPath;
							course.screenshot = screenshotPath;

				            // Write course current info
				            fs.write(courseLinksPath, JSON.stringify(data, null, 3), 'w');

				            // Continue
				            finallyFn();
			        	}, 3 * 1000);
			        }, function(elapsed, finallyFn) {
			        	// If the page cannot be loaded, pass it
						console.log('Skipping course ' + uniqueName);

			            // Continue
			            finallyFn();
			        }, function() {
			            // Continue the work
			            goNext();
					}, timeoutMs, refreshMs
			    );
			}
		});
	}
}

openLink(data, 0, 0, 0);
