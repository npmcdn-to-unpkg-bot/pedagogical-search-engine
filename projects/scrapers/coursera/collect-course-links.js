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

// source: http://stackoverflow.com/questions/16716753/how-to-download-images-from-a-site-with-phantomjs
function getImgDimensions($i) {
    return {
        top : $i.offset().top,
        left : $i.offset().left,
        width : $i.width(),
        height : $i.height()
    }
}

// Handling in/out put
var data = JSON.parse(fs.read('output/subdomain-links.txt'));
var outputPath = 'output/course-links.txt';

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
var timeoutMs = 60 * 60 * 1000;
var refreshMs = 1 * 500;
var courseraDomain = 'https://www.coursera.org';
var appended = '?languages=en';
var screenshotsPath = 'output/courses/list-screenshots';
var imagesPath = 'output/courses/miniatures';

function closeAndExit(page) {
	// Finally, quitting
	console.log('Quitting Script');
	page.close();
	phantom.exit();
}
function openLink(data, domainPosition, subdomainPosition) {
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
	var link = courseraDomain + list.href;

	page.open(link, function(status) {
		// Check for page load success
		if(status !== 'success') {
			console.log("Unable to access network, status: " + status);
		} else {
			var linkSel = 'a.rc-OfferingCard';
			var labelSel = 'a.rc-OfferingCard h2';
			var partnerSel = 'a.rc-OfferingCard .offering-partner-names';
			var imgSel = 'a.rc-OfferingCard .offering-image';

	        // Wait for page-load
	        waitFor(
	        	function(elapsed) {
	        		// Test if the page has loaded
		            return page.evaluate(function(linkSel) {
		            	var courses = $(linkSel);
		            	return courses.length > 0;
		            }, linkSel);
		        }, function(elapsed) {
		        	// When we think that the page has loaded
		        	// ..
		        	// wait an additional time
		        	setTimeout(function() {
			        	// Get the courses
						var courses = page.evaluate(function(linkSel, labelSel, partnerSel, imgSel,
							getImgDimensions) {
							var hrefs = $.map($(linkSel), function(e) {
								var href = $(e).attr('href');
								return href;
							});
							var labels = $.map($(labelSel), function(e) {
								var label = $(e).text();
								return label;
							});
							var partners = $.map($(partnerSel), function(e) {
								var partner = $(e).text();
								return partner;
							});
							var imgRemotes = $.map($(imgSel), function(e) {
								var imgSrc = $(e).attr('src');
								return imgSrc;
							});
							var imgDims = $.map($(imgSel), function(e) {
								var dim = getImgDimensions($(e));
								return dim;
							});
							var zip = [];
							for(var i = 0; i < hrefs.length; i++) {
								zip[i] = {
									href: hrefs[i]
								};
								if(i < labels.length) {
									zip[i].label = labels[i];
								}
								if(i < partners.length) {
									zip[i].partner = partners[i];
								}
								if(i < imgRemotes.length) {
									zip[i].remoteImg = imgRemotes[i];
								}
								if(i < imgDims.length) {
									zip[i].imgDims = imgDims[i];
								}
							}
							return zip;
						}, linkSel, labelSel, partnerSel, imgSel, getImgDimensions);

						// Then
						// ..
						// Take the images
						for(var i = 0; i < courses.length; i++) {
							var course = courses[i];
							var imgName = uniqueName + '-' + i + '.png';

				            page.clipRect = course.imgDims;
				            page.render(imagesPath + '/' + imgName, {
				            	format: 'png',
				            	quality: '100'
				            });

				            course.imgDims = undefined;
				            course.localImg = imgName;
						}

						// Save the links
						list.courses = courses;
			            console.log(courses.length + ' courses for "' + list.label + '" successfully collected');

						// Screenshot for info
						page.clipRect = {
							top: 0,
							left: 0,
							  width: width,
							  height: height
						};
						page.render(screenshotsPath + "/" + list.label + ".png", {
				            	format: 'png',
				            	quality: '100'
				        });

			            // Write course info
			            fs.write(outputPath, JSON.stringify(data, null, 3), 'w');

			            // Continue the work
			            var goToNextDomain = true;
			            // Check if we are in a subdomain
			            if(typeof subdomain != "undefined") {
		        			// Check if there are other subdomains
		        			if((subdomainPosition + 1) < domain.subdomain.length) {
		        				openLink(data, domainPosition, subdomainPosition + 1);
		        				goToNextDomain = false;
		        			}
			            }

			            if(goToNextDomain) {
			            	// Check if there is a next domain
			            	if((domainPosition + 1) < data.length) {
			            		openLink(data, domainPosition + 1, 0);
			            	} else {
					            // Quit
				            	closeAndExit(page);
			            	}
			            }
		        	}, 2 * 1000);
		        }, function(elapsed) {
		        	// If the page cannot be loaded
		        	console.error("Cannot load the course list: " + 
		        		list.label + "(" + list.href + ")");
		        	page.render(screenshotsPath + "/" + "error.png");
		        	closeAndExit(page);
		        }, function() {
		        	// nothing here
				}, timeoutMs, refreshMs
		    );
		}
	});
}

openLink(data, 0, 0);
