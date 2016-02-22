var fs = require('fs');

// Includes
var includesPath = "./include";
require(includesPath + '/wait-for.js');

// Handling output
var outputPath = 'courses-links.txt';
console.log('Opening ' + outputPath);
var output = fs.open('courses-links.txt', {
	mode: 'w',
	charset: 'UTF-8'
});

// Collecting Job
// todo

// Quitting
console.log('Quitting Script');
output.close();
phantom.exit();