#!/bin/bash
# This script performs a (launch-wait) loop.
# The loop exits when the script has terminated.
function display_help_and_quit {
	cat << EOF
Here is some help for you..
Script params:
	-s .. (or --script ..)
		The script path to launch
	-l .. (or --log ..)
		Path of the log file. Default: log.txt
	-h (or --help)
		show this help
EOF
	exit 1
}

# -----------------------------------------------------------------------------------
# Get script params
logPath="log.txt"
while [[ $# > 0 ]]
do
	key="$1"
	case $key in
	    -s|--script)
		    script="${2}"
			shift
		    ;;
	    -l|--log)
		    logPath="${2}"
			shift
		    ;;
	    -h|--help)
		    display_help_and_quit
		    ;;
	    *)
	        echo -e "Unknown param $1"
		    display_help_and_quit
	    	;;
	esac
	shift # past argument or value
done

if [ -z $script ] ; then
	echo -e "error: You have to specify the -s option."
	display_help_and_quit
fi

# -----------------------------------------------------------------------------------
# Main code:
#
# Create the log file
if [ ! -f "${logPath}" ]; then
    touch "${logPath}"
fi

# While the script hasn't finished
logPrepand='[script.sh]';
stay_in_loop=1
while [ $stay_in_loop -ne 0 ]; do
	# Launch the script in background
	msg="${logPrepand} Lauching the process"
	echo $msg
	echo $msg >> "${logPath}"
	phantomjs "${script}" >> "${logPath}" &
	last_pid=$!

	# Wait
	wait $last_pid

	# Test if the script has finished
	if [ -f "finished.info" ]; then
		msg="${logPrepand} finished.info exists!"
		echo $msg
		echo $msg >> "${logPath}"

		# Leave the loop
		stay_in_loop=0
	fi
 done

msg="${logPrepand} Ending!"
echo $msg
echo $msg >> "${logPath}"

exit 0
