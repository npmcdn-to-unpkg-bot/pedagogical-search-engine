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

// ReplaceAll
// source: http://stackoverflow.com/questions/1144783/replacing-all-occurrences-of-a-string-in-javascript
function replaceAll(str, search, replacement) {
    return str.replace(new RegExp(search, 'g'), replacement);
};

// Handling output
var domains = JSON.parse(fs.read('output/domain-links.txt'));

var outputPath = 'output/subdomain-links.txt';
console.log('Opening ' + outputPath);
var output = fs.open(outputPath, {
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
/* Uncomment for log messages in .evaluate sections
page.onConsoleMessage = function(msg) {
  console.log(msg);
};
// */

// Open each domain link
var timeoutMs = 20 * 1000;
var refreshMs = 1 * 1000;
var courseraDomain = 'https://www.coursera.org';
var appended = '?languages=en';
var screenshotPath = 'screenshot';

function closeAndExit(page, output) {
	// Finally, quitting
	console.log('Quitting Script');
	page.close();
	output.close();
	phantom.exit();
}
function openDomains(domains, position) {
	var domain = domains[position];
	var link = courseraDomain + domain.href;

	page.open(link, function(status) {
		// Check for page load success
		if(status !== 'success') {
			console.log("Unable to access network, status: " + status);
		} else {
			var seeAllSel = '.see-all-button';
			var labelSel = '.rc-SubdomainSampler .headline-3-text';
			var subdomainSel = '.rc-SubdomainsIndex';
			var courseSel = '.offering-content';
	        // Wait for page-load
	        waitFor(
	        	function(elapsed) {
	        		// Test if the page has loaded
		            return page.evaluate(function(seeAllSel, labelSel, subdomainSel, courseSel) {
		            	var subdomains = $(subdomainSel);
		            	if(subdomains.length == 0) {
			            	var courses = $(courseSel);
		            		return (courses.length > 5);
		            	} else {
			            	var links = $(seeAllSel);
			            	var labels = $(labelSel);
			                return (links.length > 0) && (labels.length > 0);
		            	}
		            }, seeAllSel, labelSel, subdomainSel, courseSel);
		        }, function(elapsed) {
		        	// When we think that the page has loaded
		        	// ..
		        	// wait an additional time
		        	setTimeout(function() {
			        	// Get the links
						var links = page.evaluate(function(seeAllSel, labelSel, subdomainSel, courseSel, replaceFn,
							domain, appended) {
							var hrefs = $.map($(seeAllSel), function(e) {
								var href = $($(e).parent()).attr('href');
								return href;
							});
							var labels = $.map($(labelSel), function(e) {
								var label = $(e).text();
								return label;
							});
							var zip = [];
							for(var i = 0; i < labels.length; i++) {
								// No "See All" button for this subdomain
								var label = labels[i];
								if(i > (hrefs.length - 1)) {
									// guess it
									var subdomainPath = replaceFn(replaceFn(label.toLowerCase(), "  ", " "), " ", "-");
									var domainPath = replaceFn(replaceFn(domain.label.toLowerCase(), "  ", " "), " ", "-");
									var href = "/browse/" + domainPath + "/" + subdomainPath + appended;
								} else {
									href = hrefs[i];
								}

								zip[i] = {
									href: href,
									label: label
								};
							}
							return zip;
						}, seeAllSel, labelSel, subdomainSel, courseSel, replaceAll, domain, appended);

						// Save the links
						if(links.length > 0) {
							domain.subdomain = [];
							for(var i = 0; i < links.length; i++) {
								var href = links[i].href;
								var label = links[i].label;

								domain.subdomain.push({
									href: courseraDomain + href,
									label: label
								});
							}
				            console.log(links.length + ' link(s) for "' + domain.label + '" successfully collected');
						} else {
							console.log('No links to collect for "' + domain.label + '"');
						}

						// Screenshot for info
						// page.render(screenshotPath + "/" + domain.label + ".png");

			            // At the end only
			            if(position == (domains.length - 1)) {
			            	// Write the results
			            	output.write(JSON.stringify(domains, null, 3));

			            	// Quit
		        			closeAndExit(page, output);
			            } else {
				            // Get the subdomains links for the other domains
				            openDomains(domains, position + 1);
			            }
		        	}, 1 * 1000);
		        }, function(elapsed) {
		        	// If the page cannot be loaded
		        	console.error("Cannot load the subdomain-page: " + 
		        		domain.label + "(" + domain.href + ")");
		        	page.render(screenshotPath + "/" + "error.png");
		        	closeAndExit(page, output);
		        }, function() {
		        	// nothing here
				}, timeoutMs, refreshMs
		    );
		}
	});
}

openDomains(domains, 0);
