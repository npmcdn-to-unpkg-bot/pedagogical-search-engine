// Generic wait-for function
function waitFor(testFn, successFn, timeOutFn, timeOutMs, repeatMs) {
    var start = new Date().getTime(),
        condition = false;
    var interval = setInterval(function() {
    	var elapsed = new Date().getTime() - start;
        if((elapsed < timeOutMs) && !condition) {
            condition = testFn(elapsed);
        } else {
            if(!condition) {
                timeOutFn(elapsed);
            } else {
                successFn();
                clearInterval(interval);
            }
        }
    }, repeatMs);
};